---
id: developer-notes
title: "Developer Notes"
---

# ZIO Module

 FrontEnd plugin ++ Server plugin ++ 1 or more ZIO plugins (1 for each scala version)

# ZIO Insight frontend 

The frontend is a single page web application that allows the user to efficiently navigate the application provided via the insight server.

The frontend has a plug-in architecture, so that modules to come can contribute their own specific views if required. 

# ZIO insight protocol

An extensible protocol used between the frontend and the server. 

## Requirements 

- Internationalization
- Plug-in architecture 
- Security (perhaps optional depending on the environment where ZIO Insight is being used)

## Technologies / primary libraries
 
- Scala 3 (Scala JS)
- ZIO 2
- Laminar
- Web Components ?? (Not sure if those are really required)

# ZIO Insight Server

The ZIO Insight server collects the data from all connected ZIO applications and provides it to 
the frontend upon request. 

## Technologies / primary libraries

- Scala 3
- ZIO 2
- ZIO HTTP 

# ZIO Insight Plugins

ZIO Insight Plug-Ins are used to instrument the applications for gathering data. They work on behalf of the server and provide the data requested. 

We need to think about the gathering strategies:

- Gather data on request only and change the parameters according to the search parameters
- Gather data and send it to the Insight server, so that the server can store and select it. (Presumably that would be a performance hit)

## Technologies / primary libraries

- All Scala Versions that ZIO 2 supports 
- ZIO Version 2 or greater

# Brain dump corner 

## Web Components 

### Simple Web Components example 
https://developer.mozilla.org/en-US/docs/Web/Web_Components/Using_templates_and_slots

### WebComponents in ScalaJS (outdated)
http://www.g-widgets.com/2018/05/02/developing-web-components-in-scala-js/

### Laminar Web components (outdated / not compiling from GH)
https://laminar.dev/examples/web-components

- Need to look at the generator - can the generator be generalized ?
- Can the generator be replaced / kickstarted with ScalaJS ?

### Simple ZIO Integration with Laminar

https://github.com/sherpal/BattleForFlatland/blob/master/frontend/src/main/scala/utils/laminarzio/Implicits.scala