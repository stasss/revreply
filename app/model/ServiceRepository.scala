package model

import java.sql.Connection
import java.util.UUID

import dbconn.JDBC

/**
  * Created by s_stashkevich on 5/25/2018.
  */

case class ServiceAccount(userId: String, serviceUuid: String,  serviceEmail: String, appName: String)

object ServiceRepository {

  def serviceSql(sql: String)(conn: Connection): Option[Seq[ServiceAccount]] = {
    val st = conn.createStatement()
    val rs = st.executeQuery(sql)
    var sa = Seq[ServiceAccount]()
    while ( rs.next() ) {
     sa = sa ++ Seq(ServiceAccount(rs.getString("USER_UUID").trim, rs.getString("UUID").trim,  rs.getString("SERVICE_EMAIL").trim, rs.getString("APP_NAME").trim))
    }
    Some(sa)
  }


  def add(sql: String)(conn: Connection): Option[Int] = {
    val st = conn.createStatement()
    Some(st.executeUpdate(sql))
  }

  def accountsByUser(user: User): Seq[ServiceAccount] ={
     JDBC.execute[Seq[ServiceAccount]]( serviceSql(s"select * from SERVICE_ACCOUNT where USER_UUID='${user.id}'")).getOrElse(Seq())
  }

  def addAccount(user: User, email: String, appName: String): Option[Int] ={
    JDBC.execute[Int]( add(s"insert into service_account (USER_UUID, UUID, SERVICE_EMAIL, APP_NAME) values ('${user.id}', '${UUID.randomUUID().toString}', '${email}', '${appName}')"))
  }

}
