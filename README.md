# schub-elasticsearch-relay

SCHub Elasticsearch Proxy adding CAS SSO authentication, visibility filtering and multi-server-splitting between Elasticsearch 1.x and 2.x instances.

Only proxies Parts of the ES search API.

Supports indices from Liferay, Nuxeo, Shindig and the Elasticsearch IMAP importer.

License: Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0

### Usage
1. Edit src/main/resources/elasticsearch-relay.properties to match your setup
2. Build a using maven build with package goal
3. A deployable war file is generated in the target-Folder 
4. Deploy in Tomcat 7 or 8