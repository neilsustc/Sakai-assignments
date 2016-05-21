import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("deprecation")
public class Sakai
{
    private static final String HOST = "weblogin.sustc.edu.cn";
    private static final String CAS_PATH = "/cas/login";
    private static final String LOGIN_SERVICE = "http://sakai.sustc.edu.cn/portal/login";

    private static String cookie;

    public static Map<String, ArrayList<HW>> getHws(String username,
            String pass)
    {
        LinkedHashMap<String, ArrayList<HW>> courseAndHws = null;

        String sakaiHomePage = login(username, pass);
        if (sakaiHomePage.equals("incorrect"))
        {
            MainWindow.current.showErrorMsg("用户名或密码不正确");
        } else if (sakaiHomePage.equals("network error"))
        {
            MainWindow.current.showErrorMsg("网络错误");
        } else
        {
            courseAndHws = new LinkedHashMap<>();
            ArrayList<String> courseLinks = getCourseLinks(sakaiHomePage);
            for (String courseLink : courseLinks)
            {
                Document coursePageDoc = getCoursePageDoc(courseLink);
                String courseName = getCourseName(coursePageDoc);
                String hwSubpageLink = getHwSubpageLink(coursePageDoc);
                if (hwSubpageLink != null)
                {
                    String hwIframeLink = getIframeLink(hwSubpageLink);
                    ArrayList<HW> hws = getHwsFromIframe(hwIframeLink);
                    // sort
                    hws.sort(new Comparator<HW>()
                    {
                        @Override
                        public int compare(HW h1, HW h2)
                        {
                            return h1.dueDateTime.compareTo(h2.dueDateTime);
                        }
                    });
                    courseAndHws.put(courseName, hws);
                }
            }
        }
        return courseAndHws;
    }

    private static String login(String username, String password)
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try
        {
            URI loginURI = new URIBuilder().setScheme("http").setHost(HOST)
                    .setPath(CAS_PATH).setParameter("service", LOGIN_SERVICE)
                    .build();
            HttpGet httpGet = new HttpGet(loginURI);
            response = httpClient.execute(httpGet);
            String loginPageStr = getResponseText(response);

            // get 'lt'
            Document loginPageDoc = Jsoup.parse(loginPageStr);
            Element loginForm = loginPageDoc.select("form#fm1").first();
            String lt = loginForm.select("input[name=lt]").first()
                    .attr("value");

            if (lt != null && !"".equals(lt))
            {
                List<BasicNameValuePair> list = new ArrayList<>();
                list.add(new BasicNameValuePair("username", username));
                list.add(new BasicNameValuePair("password", password));
                list.add(new BasicNameValuePair("lt", lt));
                list.add(new BasicNameValuePair("execution", "e1s1"));
                list.add(new BasicNameValuePair("_eventId", "submit"));
                list.add(new BasicNameValuePair("submit", "LOGIN"));
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list);
                HttpPost httpPost = new HttpPost(loginURI);
                httpPost.setEntity(entity);
                response = httpClient.execute(httpPost);
                if (getResponseText(response)
                        .contains("cannot be determined to be authentic"))
                {
                    return "incorrect";
                }

                setDefaultHeader(httpGet);
                response = httpClient.execute(httpGet);

                httpGet = new HttpGet(
                        response.getFirstHeader("Location").getValue());
                setDefaultHeader(httpGet);
                response = httpClient.execute(httpGet);
                cookie = response.getLastHeader("Set-Cookie").getValue();

                httpGet = new HttpGet(
                        response.getFirstHeader("Location").getValue());
                httpGet.setHeader("Cookie", cookie);
                setDefaultHeader(httpGet);
                response = httpClient.execute(httpGet);

                httpGet = new HttpGet(
                        response.getFirstHeader("Location").getValue());
                httpGet.setHeader("Cookie", cookie);
                setDefaultHeader(httpGet);
                response = httpClient.execute(httpGet);

                String sakaiHomePage = getResponseText(response);
                return sakaiHomePage;
            } else
            {
                System.err.println("Login page not found");
            }
        } catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
        } finally
        {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpClient);
        }
        return "network error";
    }

    private static ArrayList<String> getCourseLinks(String homePage)
    {
        ArrayList<String> links = new ArrayList<>();

        Document sakaiHomeDoc = Jsoup.parse(homePage);
        Elements courseLis = sakaiHomeDoc.select("li.nav-menu");
        for (Element element : courseLis)
        {
            Element link = element.select("a").first();
            String href = link.attr("href");
            if (!href.equals("#"))
                links.add(href);
        }
        return links;
    }

    private static Document getCoursePageDoc(String courseLink)
    {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        configureHttpClient2(httpClientBuilder);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpGet httpGet = new HttpGet(courseLink);
        httpGet.setHeader("Cookie", cookie);
        CloseableHttpResponse response = null;

        String html = null;
        try
        {
            response = httpClient.execute(httpGet);
            html = getResponseText(response);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpClient);
        }
        return Jsoup.parse(html);
    }

    private static String getCourseName(Document doc)
    {
        return doc.select("li.nav-selected span").first().text();
    }

    private static String getHwSubpageLink(Document doc)
    {
        Elements toolMenus = doc.select("a.toolMenuLink");
        for (Element tool : toolMenus)
        {
            String toolName = tool.select("span.menuTitle").first().text();
            if (toolName.contains("作业") || toolName.contains("Assignments"))
            {
                return tool.attr("href");
            }
        }
        return null;
    }

    private static String getIframeLink(String hwSubpageLink)
    {
        String html = doGet(hwSubpageLink);
        Document hwListPageDoc = Jsoup.parse(html);
        Element iframe = hwListPageDoc.select("iframe").first();
        return iframe.attr("src");
    }

    private static ArrayList<HW> getHwsFromIframe(String iframeLink)
    {
        ArrayList<HW> hws = new ArrayList<>();

        String html = doGet(iframeLink);
        Document hwListPageDoc = Jsoup.parse(html);
        Element tbody = hwListPageDoc.select("tbody").first();
        Elements trs = tbody.children();
        // remove the table header
        trs.remove(0);
        for (Element tr : trs)
        {
            String title = tr.select("td[headers=title] a").first().text();
            String status = tr.select("td[headers=status]").first().text();
            String dueDateTime = tr.select("td[headers=dueDate] span").first()
                    .text();
            hws.add(new HW(title, status, dueDateTime));
        }
        return hws;
    }

    // ----- Others -----

    private static void setDefaultHeader(HttpRequestBase httpGet)
    {
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.protocol.handle-redirects",
                Boolean.valueOf(false));
        httpGet.setParams(params);
    }

    private static String getResponseText(CloseableHttpResponse response)
            throws IOException
    {
        String string = String.join("",
                IOUtils.readLines(response.getEntity().getContent(), "utf-8"));
        return string;
    }

    private static void configureHttpClient2(HttpClientBuilder clientBuilder)
    {
        try
        {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager()
            {
                public void checkClientTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException
                {
                }

                public void checkServerTrusted(X509Certificate[] chain,
                        String authType) throws CertificateException
                {
                }

                public X509Certificate[] getAcceptedIssuers()
                {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            clientBuilder.setSSLContext(ctx);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String doGet(String link)
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        HttpGet httpGet = new HttpGet(link);
        httpGet.setHeader("Cookie", cookie);

        String html = null;

        try
        {
            response = httpClient.execute(httpGet);
            html = getResponseText(response);
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            IOUtils.closeQuietly(response);
            IOUtils.closeQuietly(httpClient);
        }
        return html;
    }
}
