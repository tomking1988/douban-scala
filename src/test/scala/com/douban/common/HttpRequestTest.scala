package com.douban.common


/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 12/26/12 10:37 PM
 * @version 1.0
 */
class HttpRequestTest extends BaseTest{
  test("the auth url"){
    val url=AuthorizationCode().authUrl
    val con= (new HttpRequest(url).get()).connection
    println(con.getURL)
  }
  test("the token url"){
    val token:Token=new Token()
    val url=token.tokenUrl
    println("token url is -->"+url)
    val http= (new HttpRequest(url).post(new Token))
    prettyJSON(http.parseJSON[AccessTokenResult]())
  }

}