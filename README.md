# MapBox map rendering tool

This is a tool for rendering static maps via [MapBox](https://mapbox.com) with a little help of
some of supported [directions APIs](#directions-api-providers) (in case of rendering path on the map).

It has no ambitions to be a published library of any type; it's supposed to be tool which you checkout to your
computer and run it from sources.  
The code is not a real best-practises overview, please don't judge me based on it :-)

## Usage

### Prerequisites

1. Installed Java  
    The tool has been developed on Java 8 but anything newer should work too.
1. MapBox API key
    You can get the _Default public token_ in your [MapBox account](https://www.mapbox.com/account/) (which you may
    need to create first ;-)
1. Directions API provider account - see [directions API providers](#directions-api-providers).
    This is optional and you will need it only if you want some directions path to be rendered into the map.  

### Download

You can either [download the project as ZIP](https://github.com/avast/cactus/archive/master.zip) or checkout this
repo with a GIT.

### Create configuration

The application is configured via single file in [HOCON format](https://github.com/lightbend/config/blob/master/HOCON.md).  

The file has to content:
1. `mapBoxApiKey`
1. `maps` configurations
1. `defaults` configuration
1. `maxPointsBase` (optional) - see [P.S.s](#pss); configures max. number of points passed to MapBox rendering

Each map configuration contains:
1. `fileName` - path to the file where the map should be saved
1. `width`, `height` - dimensions of the map image (read about limitations in [docs](https://www.mapbox.com/api-documentation/#retrieve-a-static-map-from-a-style))
1. `zoom`
1. `pitch` - angle of map view
1. `center` - center point of the map
1. `path` (optional) - set of places for directions path rendering; if present, has to contains at least 2 places
1. `mapPoints` (optional) - set of places where to put map markers

Path configuration:
1. `directionsProvider` - Directions API configuration
1. waypoints - set of places for directions path rendering
    * has to contains at least 2 places
    * format of waypoints depends on specific directions provider

Directions API configuration:
1. `provider` - name of provider, see [supported providers list](#directions-api-providers)
1. `mode` - mode of the path searching; directly related to specific provider
1. `apiKey` - API key of specific provider; see related section in [supported providers list](#directions-api-providers)

Defaults configuration:
To prevent repetition of parameters you usually want to have the same in all maps, there is `defaults` section in the config.  
It _may_ be completely empty if you don't want to use it or it supports all declarations like _map configuration_ (it's merged as
fallback).

_Note: you can use [playground](https://www.mapbox.com/help/static-api-playground/) to try map render settings._

Example file looks like this:

```hocon
mapBoxApiKey = "pk.*************************************************************************************dA"
googleApiKey = "AI***********************************OA"

defaults {
  style = "satellite-streets-v10"
  width = 1280
  height = 720
  zoom = 5.9
  pitch = 30

  path {
    directionsProvider {
      provider = "google"
      mode = "driving"
      apiKey = ${googleApiKey}
    }
  }
}

points = [
  {
    name = "Canterbury Cathedral",
    coords {
      lat = 51.280377
      lon = 1.0809668
    }
  },
  {
    name = "Holyhead",
    coords {
      lat = 53.3094391
      lon = -4.6328815
    }
  }
]

maps = [{
  fileName = "/data/maps/wales-south.jpeg"
  width = 1280
  height = 720
  zoom = 8.5
  pitch = 30
  
  center {
    lat = 51.73153
    lon = -4.03355
  }
  
  path {
    directionsProvider {
      provider = "google"
      mode = "driving"
      apiKey = ${googleApiKey}
    }
    
    waypoints = [
      "cardiff, wales",
      "st.davids, wales",
      "fishguard, wales",
      "tenby, wales"
    ]
  }
  
  mapPoints = ${points}
}, {
  fileName = "/data/maps/wales-north.jpeg"
  width = 1280
  height = 720
  zoom = 8.5
  pitch = 30
  
  center {
    lat = 52.73441
    lon = -3.82024
  }
  
  // missing path
  
  mapPoints = ${points}
}, {
  fileName = "/data/maps/pembrokeshire-coast-path.jpeg"
  style = "satellite-streets-v10"
  width = 1280
  height = 720
  zoom = 10.8
  pitch = 3
  
  center {
    lat = 51.93869
    lon = -5.14742
  
  // "hiking" path:
  path {
    directionsProvider {
      provider = "openrouteservice"
      mode = "foot-hiking"
      apiKey = "5b****************************************************af"
    
    waypoints = [
      "-5.10672,51.96083",
      "-5.163638,51.947461",
      "-5.0833697,52.0204133",
      "-4.9915736,52.0206869",
      "-4.9954322,52.0050561"
    ]
  }
}]
```

_Note: There is substitution used in the example above. I think it may be quite often what you need in this case._


### Run it

The simplest way to run it is to use the Gradle wrapper:

```bash
./gradlew run --args="input.conf"
```

where _`input.conf`_ is your configuration file.

If rendering of sam map fails with `HTTP 413` from MapBox, try to specify `maxPointsBase` in the configuration. It will lower your points
in rendered path but raise the probability to actually render the map. WTH, you ask? See [P.S.s](#pss).

### P.S.s

Some genius in MapBox decided that the static maps API will accept only `GET` requests, which may lead to
problem - requests are about to fail because the request line is too long.

Another issue is they are limiting number of waypoints when rendering path. It's not mentioned anywhere in docs
however requests with more than _`N`_ (TBH I didn't manage to discover exact _`N`_...) waypoints end with HTTP 413 ("Payload Too Large") error. This is the reason why
waypoints returned by Google are filtered to satisfy the limit. Unfortunately it may happen (if you try to render
very long path) the filtering will be too strong which will cause killing the path fluency (didn't happen to me).

## Directions API providers

Recommendations:
* for car routes, use Google
* for walking/hiking routes, use OpenRoute Service

### Google

It's free.  
Learn how to get the key at [Google Developers page](https://developers.google.com/maps/documentation/directions/get-api-key).
See [docs of Google Directions API](https://developers.google.com/maps/documentation/directions/intro) for more info (e.g. route
planning modes).  
Waypoints are geocoded if needed so you can use all coordinates (`lat,lon`, e.g. `41.43206,-81.38992` meaning "41.43206N, 81.38992W"),
addresses and place IDs. Read more in docs. 

### OpenRoute Service

It's free.  
Register at [OpenRoute Service web](https://openrouteservice.org/dev/#/signup) to get an API key and then discover available
route planning modes in [Directions Service docs](https://openrouteservice.org/dev/#/api-docs/directions/get).  
Waypoints are in `lon:lat` format (e.g. `-5.10672,51.96083` meaning "51.96083N, 5.10672W"). You can get the waypoints e.g. at
[OpenRoute Service Map](https://maps.openrouteservice.org/).

## TODOs

1. Parametrize rest of map rendering options - bearing, retina
