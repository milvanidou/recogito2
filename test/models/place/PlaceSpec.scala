package models.place

import com.vividsolutions.jts.geom.{ Coordinate, GeometryFactory }
import org.specs2.mutable._
import org.specs2.runner._
import org.joda.time.{ DateTime, DateTimeZone }
import org.junit.runner._
import play.api.Logger
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.Json
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class PlaceSpec extends Specification {
  
  "sample place" should {
    
    "be properly created from JSON" in {
      val json = Source.fromFile("test/resources/place.json").getLines().mkString("\n")
      val parseResult = Json.fromJson[Place](Json.parse(json))
      
      Logger.info(parseResult.toString)
      
      parseResult.isSuccess must equalTo(true)
      
      val place = parseResult.get
      
      place.id must equalTo ("http://pleiades.stoa.org/places/118543")
      place.title must equalTo("Ad Mauros")

      val location = new Coordinate(14.02358, 48.31058)
      place.representativePoint must equalTo(Some(location))
      place.geometry must equalTo(Some(new GeometryFactory().createPoint(location)))

      val from = new DateTime(DateTimeZone.UTC).withDate(-30, 1, 1).withTime(0, 0, 0, 0)
      val to = new DateTime(DateTimeZone.UTC).withDate(640, 1, 1).withTime(0, 0, 0, 0)
      place.temporalBounds must equalTo(Some(TemporalBounds(from, to)))

      /*
      val expectedURIs = Seq(
          "http://pleiades.stoa.org/places/118543",
          "http://dare.ht.lu.se/places/10778",
          "http://www.trismegistos.org/place/35191")          
      place.uris must containAllOf(expectedURIs)



      place.placeTypes.size must equalTo(2)
      place.placeTypes.map(_.placeType) must containAllOf(Seq("fort", "tower"))
      
      place.descriptions.size must equalTo(1)
      place.descriptions.head must equalTo(Description("An ancient place, cited: BAtlas 12 H4 Ad Mauros", None, Gazetteer("Pleiades")))
      
      val expectedNames = Seq(
          Name("Ad Mauros", None, Seq(Gazetteer("Pleiades"), Gazetteer("Trismegistos"))),
          Name("Ad Mauros/Marinianio, Eferding", None, Seq(Gazetteer("DARE"))),
          Name("Eferding", None, Seq(Gazetteer("Trismegistos"))),
          Name("Marianianio", Some("la"), Seq(Gazetteer("Trismegistos"))))
          
      place.names must containAllOf(expectedNames)

      val expectedCloseMatches = Seq(
        "http://sws.geonames.org/2780394",
        "http://www.wikidata.org/entity/Q2739862",
        "http://de.wikipedia.org/wiki/Kastell_Eferding",
        "http://www.cambridge.org/us/talbert/talbertdatabase/TPPlace1513.html")
        
      place.closeMatches must containAllOf(expectedCloseMatches)
      place.exactMatches.size must equalTo(0)
      */
    }
    
  }
    
  "JSON serialization/parsing roundtrip" should {
    
    "yield an equal Place" in {
      // TODO create Place programmatically
      
      // TODO serialize place to JSON
      
      // TODO parse JSON
      
      // TODO compare parsed Place with original place - must be equal
      success
    }
    
  }
  
  // TODO test index insertion (i.e. does the ES mapping definition fit the test JSON? 
    
}