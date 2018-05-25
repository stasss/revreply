package dbconn

import java.sql.{Connection, DriverManager, ResultSet}

import scala.util.Try

object JDBC {

  def execute[T]( fn: Connection => Option[T]): Option[T] = {
    // there's probably a better way to do this
    var connection:Connection = null
    try {
      Class.forName("org.postgresql.Driver")
      connection = DriverManager.getConnection("jdbc:postgresql://ec2-46-137-94-97.eu-west-1.compute.amazonaws.com:5432/d7m8o1dugerelp?sslmode=require", "oapozqjfmncnbi", "b35e3a354cbebc54e99cd2b319333b947f7e0c97f86ee3129bd8f18383926193")
      // create the statement, and run the select query
      fn(connection)
    } catch {
      case e => e.printStackTrace
      None
    }finally {
      None
    }
  }
}