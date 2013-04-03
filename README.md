# LuckyBird's Mozilla Ignite Demo

A web application written in Clojure and ClojureScript to help the SFMTA determine where various Muni routes slow down, where additional buses are needed, etc.

## Live Demo

A live demo is available at [http://luckybird-ignite-demo.herokuapp.com][1].

[1]: http://luckybird-ignite-demo.herokuapp.com

## Prerequisites

You will need [Leiningen][2] 2.0 or above installed.

You will need Postgres installed.

[2]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

	lein ring server

You must set the URL for your postgres database in the enviromnent variable DATABASE_URL.
e.g 

	DATABASE_URL=postgresql://username:password@host:port/db_name

You must also set a Google Static Maps API Key in the environment variable GOOGLE_MAPS_API_KEY.
	
## Documentation

[API Docs][3]

[3]: http://cammsaul.github.com/mozilla-ignite-demo

## License

Copyright © 2013 LuckyBird Inc.
