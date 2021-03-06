package com.douban.models

import com.douban.common.Req._
import com.douban.common.Req
import java.net.URLEncoder
import java.util.Date
import scala.Predef._
import scala._
import com.google.gson.JsonElement
import scala.collection.JavaConverters._
import java.util
import scala.collection.mutable

/**
 * Copyright by <a href="http://crazyadam.net"><em><i>Joseph J.C. Tang</i></em></a> <br/>
 * Email: <a href="mailto:jinntrance@gmail.com">jinntrance@gmail.com</a>
 * @author joseph
 * @since 1/3/13 10:46 PM
 * @version 1.0
 */
trait Bean extends Serializable{
  protected var _files: Map[String, String] = Map()

  def files_=(fs: Map[String, String]) {
    _files = fs
  }

  def files = _files

  /**
   * 将Bean与一个url组合
   * @param urlPrefix 请求的原始路径
   * @param bean 需要参数话的Bean
   * @return  含参数的url
   */
  def flatten(urlPrefix: String, bean: Bean = this): String = {
    urlPrefix + "?" + toParas
  }

  /**
   *
   * @return 把Bean转化为key=value&key1=value1的序列 ,添加apikey
   */
  def toParas: String = s"apikey=$apiKey&" + flat(Req.g.toJsonTree(this))

  /**
   * 层级参数全部flattened 成一层的key-value形式，
   * List的values用 n=value,n=1,2,3,4
   */
  private def flat(json: JsonElement): String = {
    val l = for {e <- json.getAsJsonObject.entrySet().asScala
    } yield {
      if (e.getValue.isJsonPrimitive) e.getKey + "=" + URLEncoder.encode(e.getValue.getAsString, Req.ENCODING)
      else if (e.getValue.isJsonObject) this.flat(e.getValue)
    }

    l.mkString("&")
  }

  /**
   *
   * @return map ,flattened fields-> values
   */
  def bean2Map(b:Any):util.Map[String,Any]={
    Req.g.toJsonTree(b).getAsJsonObject.entrySet().asScala.foldLeft(mutable.Map[String,Any]()){
      case (a,e)=>
        if (e.getValue.isJsonPrimitive) a + (e.getKey -> e.getValue.getAsString)
        else  if (e.getValue.isJsonArray)  a+(e.getKey -> e.getValue.getAsJsonArray.iterator().asScala.mkString(","))
        else if (e.getValue.isJsonObject)  a++ bean2Map(e.getValue).asScala
        else a
    }.asJava
  }
  def bean2Map:util.Map[String,Any]=bean2Map(this)
}

abstract class API[+B: Manifest] {
  var secured = false
  val api_prefix = "https://api.douban.com/v2/"
  val shuo_prefix = "https://api.douban.com/shuo/v2/"
  val bub_prefix = "http://api.douban.com/labs/bubbler/"

  protected def url_prefix: String

  protected val idUrl = url_prefix + "/%s"


  /**
   * 通过id获取
   */
  def byId(id: Long) = get[B](idUrl.format(id), secured = true)

}

abstract class BookMovieMusicAPI[+B: Manifest, +RT: Manifest, +RV: Manifest] extends API[B] {
  private val popTagsUrl = url_prefix + "%s/tags"
  private val reviewsPostUrl = url_prefix + "reviews"
  private val reviewUpdateUrl = url_prefix + "review/%s"

  protected def searchUrl = url_prefix + "/search"

  private val tagsUrl = url_prefix + "user_tags/%s"

  /**
   * 获取某个Item中标记最多的标签
   */
  def popTags(id: Long) = get[TagsResult](popTagsUrl.format(id))

  /**
   * 发表新评论
   */

  def postReview[R <: ReviewPosted](r: R, withResult: Boolean = true) = post[RV](reviewsPostUrl, r, withResult)

  /**
   * 修改评论
   */
  def updateReview[R <: ReviewPosted](reviewId: Long, r: R, withResult: Boolean = true) = put[RV](reviewUpdateUrl.format(reviewId), r, withResult)


  /**
   * 删除评论
   */
  def deleteReview(reviewId: Long): Boolean = delete(reviewUpdateUrl.format(reviewId))


  /**
   * 获取用户对Items的所有标签
   */
  def tagsOf(userId: Long) = get[TagsResult](tagsUrl.format(userId))

  /**
   * 搜索，query/tag必传其一
   * @param query  查询关键字
   * @param tag   查询标签
   * @param page  显示第几页
   * @param count  每页显示数量
   * @return
   */
  def search(query: String, tag: String, page: Int = 1, count: Int = 20) = get[RT](new Search(query, tag, (page - 1) * count, count).flatten(searchUrl), secured = true)

}

/**
 * 标签信息
 */
case class Tag(count: Int, title: String)  extends Bean

/**
 *
 * @param q 查询关键字
 * @param start 开始数量
 * @param count 返回总数
 * @param tag  图书 电影可以传tags
 */
case class Search(q: String, tag: String, start: Long = 0, count: Int = 20) extends Bean

/**
 *
 * @param max  5 最大值
 * @param min 0  最小值
 * @param value  評分
 */
case class ReviewRating(max: Int, min: Int, value: String)   extends Bean

/**
 * @param max 10 評分結果最大值
 * @param min  0 評分結果最小值
 * @param average 平均评分
 * @param numRaters 评分人数
 */
case class ItemRating(max: Int, min: Int, average: String, numRaters: Int)  extends Bean

class ListResult(start: Int, count: Int, total: Int)  extends Bean

class ListSearch(start:Int=0,count:Int=20) extends Bean
case class ListSearchPara(start:Int=0,count:Int=20) extends Bean

class Review(id: Long, title: String, alt: String, author: User, rating: ReviewRating,
             votes: Int, useless: Int, comments: Int, summary: String, published: Date, updated: Date)  extends Bean

/**
 *
 * @param title 标题
 * @param content 内容
 * @param rating  打分1-5 //TODO no ever case class
 */
class ReviewPosted protected(title: String, content: String, rating: Int = 0) extends Bean

