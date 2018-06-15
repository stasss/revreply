package model

import java.sql.Connection
import java.util.UUID

import dbconn.JDBC

/**
  * Created by s_stashkevich on 5/25/2018.
  */

case class ServiceAccount(userId: String, serviceUuid: String,  appName: String, jsonKey: String, mode: String)

object ServiceRepository {

  def serviceSql(sql: String)(conn: Connection): Option[Seq[ServiceAccount]] = {
    val st = conn.createStatement()
    val rs = st.executeQuery(sql)
    var sa = Seq[ServiceAccount]()
    while ( rs.next() ) {
     sa = sa ++ Seq(ServiceAccount(rs.getString("USER_UUID").trim, rs.getString("UUID").trim,  rs.getString("APP_NAME").trim, rs.getString("JSON_KEY").trim, rs.getString("MODE").trim))
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

  def addAccount(user: User, appName: String, jsonKey: String): Option[Int] ={
    JDBC.execute[Int]( add(s"insert into service_account (USER_UUID, UUID, APP_NAME, JSON_KEY, MODE) values ('${user.id}', '${UUID.randomUUID().toString}', '${appName}', '${jsonKey}' , 'test')"))
  }

}
