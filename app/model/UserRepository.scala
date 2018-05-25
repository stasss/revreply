package model

import java.sql.Connection

import dbconn.JDBC

/**
  * Created by s_stashkevich on 5/25/2018.
  */

case class User(id: String, token: String, password: String)

object UserRepository {

  def loginSql(sql: String)(conn: Connection): Option[User] = {
     val st = conn.createStatement()
     val rs = st.executeQuery(sql)
     var users = Seq[User]()
     while ( rs.next() ) {
      val id = rs.getString("uuid").trim
      val name = rs.getString("name").trim
      val pwd  = rs.getString("password").trim
      users = users ++ Seq(User(id.toString, name, pwd ))
     }

     if(users.length > 1){
       throw new Exception("Database exception")
     }
     else if(users.length == 1){
       Some(users(0))
     }else{
       None
     }
  }

  def login(name: String, password: String): Option[User] = {
     JDBC.execute[User](loginSql(s"select * from users where name = '${name}' and password = '${password}'"))
  }

  def userExists(id: String): Option[User] = {
    JDBC.execute[User](loginSql(s"select * from users where uuid = '${id}'"))
  }

}
