# LuckyBird's Mozilla Ignite Demo

A web application written in Clojure to help the SFMTA determine where various Muni routes slow down, where additional buses are needed, etc.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.
You will need Postgres installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server
	
You must set the URL for your postgres database in the enviromnent variable DATABASE_URL.
e.g 
    DATABASE_URL=postgresql://username:password@host:port/db_name
	
## Documentation


## License

Copyright © 2013 LuckyBird Inc.
