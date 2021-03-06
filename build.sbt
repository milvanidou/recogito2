name := """recogito2"""

version := "2.2"

scalaVersion := "2.11.11"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalacOptions ++= Seq("-feature")

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/",
  "http://maven.geotoolkit.org/" at "http://maven.geotoolkit.org/",
  "Atlassian Releases" at "https://maven.atlassian.com/public/"
)

libraryDependencies ++= Seq(
  jdbc,
  ehcache,
  filters,
  guice,

  "com.mohiva" %% "play-silhouette" % "5.0.0",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0" % Test,
  "com.mohiva" %% "play-silhouette-cas" % "5.0.0",

  "com.nrinaudo" %% "kantan.csv" % "0.2.1",
  "com.nrinaudo" %% "kantan.csv-commons" % "0.2.1",

  "com.sksamuel.elastic4s" %% "elastic4s-core" % "5.6.1",
  "com.sksamuel.elastic4s" %% "elastic4s-tcp" % "5.6.1",

  "com.typesafe.akka" %% "akka-testkit" % "2.5.8" % Test,

  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1",
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",

  "com.vividsolutions" % "jts" % "1.13",

  "commons-io" % "commons-io" % "2.4",

  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.5.2" classifier "models",

  "eu.bitwalker" % "UserAgentUtils" % "1.20",

  "net.codingwell" %% "scala-guice" % "4.1.1",

  "org.apache.jena" % "jena-arq" % "3.1.0",

  "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.8.2",

  "org.geotools" % "gt-geojson" % "14.3",

  "org.jooq" % "jooq" % "3.7.2",
  "org.jooq" % "jooq-codegen-maven" % "3.7.2",
  "org.jooq" % "jooq-meta" % "3.7.2",

  "org.jooq" % "joox" % "1.5.0",

  "org.postgresql" % "postgresql" % "9.4.1208.jre7",

  "org.webjars" %% "webjars-play" % "2.6.2",

  // Scalagios core + transient dependencies
  "org.pelagios" % "scalagios-core" % "2.0.1" from "https://github.com/pelagios/scalagios/releases/download/v2.0.1/scalagios-core.jar",
  "org.openrdf.sesame" % "sesame-rio-n3" % "2.7.5",
  "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.7.5",

  // Recogito plugin API
  "org.pelagios" % "recogito-plugin-sdk" % "0.0.2" from "https://github.com/pelagios/recogito2-plugin-sdk/releases/download/v0.0.2/recogito-plugin-sdk-0.0.2.jar",

  "org.webjars" % "dropzone" % "4.2.0",
  "org.webjars" % "jquery" % "1.12.0",
  "org.webjars" % "jquery-ui" % "1.11.4",
  "org.webjars" % "leaflet" % "1.3.1",
  "org.webjars" % "numeral-js" % "1.5.3-1",
  "org.webjars" % "openlayers" % "4.5.0",
  "org.webjars" % "papa-parse" % "4.1.0-1",
  "org.webjars" % "requirejs" % "2.1.22",
  "org.webjars" % "slick" % "1.6.0",
  "org.webjars" % "typeaheadjs" % "0.11.1",
  "org.webjars" % "velocity" % "1.1.0",
  "org.webjars.bower" % "marked" % "0.3.6",
  "org.webjars.bower" % "js-grid" % "1.4.1",
  "org.webjars.bower" % "plotly.js" % "1.12.0",
  "org.webjars.bower" % "rangy" % "1.3.0",
  "org.webjars.bower" % "timeago" % "1.4.1",
  "org.webjars.npm" % "chartist" % "0.9.8",

  specs2 % Test
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs, digest, gzip)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

unmanagedJars in Runtime ++= Attributed.blankSeq((file("plugins/") ** "*.jar").get)

val generateJOOQ = taskKey[Seq[File]]("Generate JooQ classes")
generateJOOQ := {
  val src = sourceManaged.value
  val cp = (fullClasspath in Compile).value
  val r = (runner in Compile).value
  val s = streams.value
  r.run("org.jooq.util.GenerationTool", cp.files, Array("conf/db.conf.xml"), s.log).failed foreach (sys error _.getMessage)
  (src ** "*.scala").get
}
