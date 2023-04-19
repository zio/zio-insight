---
id: index
title: "Introduction to ZIO Insight"
sidebar_label: "ZIO Insight"
---

@PROJECT_BADGES@

# An Introduction to ZIO Insight

**A Comprehensive Tool for Monitoring and Visualizing ZIO Applications**

## Introduction

As Scala developers continue to explore the vast possibilities of the ZIO ecosystem, the need for efficient and comprehensive monitoring tools becomes increasingly important. With this in mind, we are thrilled to introduce ZIO Insight, a new open-source project that offers a powerful and intuitive solution for monitoring and visualizing ZIO applications.

ZIO Insight comprises a server and a client component. The server component is a library that can be easily integrated into your ZIO application, while the client component is a standalone application that can be run locally or deployed to a server.

The server component collects and exposes metrics and fiber traces through a REST API, while the client component renders this information in a user-friendly manner via charts and navigable trees.

## ZIO Insight goals and non-goals

ZIO Insight aims to be a lightweight and easy-to-use tool for monitoring and visualizing ZIO applications. It is not designed to serve as a comprehensive monitoring solution, but rather as a supplementary tool for developers seeking to gain a deeper understanding of their ZIO application's performance.

As such, its primary use cases are during development and testing phases.

We will provide modules with ZIO-specific content and strive to minimize overlap with other tools such as Grafana or DataDog.

## ZIO Insight module overview

As it is still in its early stage, ZIO Insight's direction and scope may evolve over time. We welcome your feedback and suggestions.

At the moment ZIO Insight will have the following modules:

- **Metrics capturing and visualization**. ZIO Insight originated as a simple metrics client for ZIO applications, enabling users to quickly view collected metrics without the need for an additional setup.

  The first release of this module is already available.

![Metrics Dashboard](../img/Metrics.png)

- **Fiber tracing and visualization**. ZIO Insight offer a means to visualize fiber traces, enabling users to observe how fibers are being scheduled, executed and how they relate to each other in a tree-like structure.

  This module is available as a preview with limited search and navigation capacities. The next step is to test the existing functionality in real life applications to refine the API.

![Fiber Tracing](../img/FiberTraces.png)

- **Service Dependency visualization**. ZIO 2 already provides support to render service dependencies in a graph at compile time. ZIO Insight will provide these dependencies at runtime and a way to visualize this graph in the browser.

  This module is not yet available.

- **Profiling support**. ZIO 2 already provides support for casual profiling of ZIO applications. We aim to support this functionality in ZIO Insight by allowing the developer to specify code locations that should be subject to casual profiling. A server module should collect the profiling data, while the client module should visualize it.

  This module is not yet available.

# Trying it out

To try ZIO Insight yourself you will need to chack out the code from GitHub for both the server and the client.

The server has a sample application in its test folder that creates some metrics and also collects fiber information using a supervisor. This is a good entry point to start exploring ZIO Insight.

**NOTE**: ZIO Insight is under heavy development and the main branches may be unstable at times. For ZIO World we have released a preview version 0.0.1 for both the server and the client. It is recommended to use these versions for exploration unless you want to start hacking on new features.

## Pre-requisites

1. To execute the server locally you need to have Java and sbt installed. You can find instructions on how to install them [here](https://www.scala-sbt.org/1.x/docs/Setup.html).

1. To execute the client locally you need to have Node.js and Yarn installed. You can find instructions on how to install them [here](https://yarnpkg.com/getting-started/install).

## Running the server with a sample application

1. Clone the repository and checkout the ´v0.0.1' tag.

   ```bash
   git clone -b v0.0.1 https://github.com/zio/zio-insight-server.git
   ```

1. Navigate to the `zio-insight-server` folder and run the following command to start the server.

   ```bash
   sbt "core/Test/runMain sample.SampleApp"
   ```

1. This will bring up the server with the embedded HTTP Server listening on port 8080. The insight API is available within this server.

## Running the client locally

1. Clone the repository and checkout the ´v0.0.1' tag.

   ```bash
   git clone -b v0.0.1 https://github.com/zio/zio-insight-ui.git
   ```

1. Navigate to the `zio-insight-ui` folder and run the following command to start the client.

   ```bash
   yarn install
   yarn dev
   ```

1. This will bring up a node development server listening on port 5173. You can access the client by navigating to http://localhost:5173.

The terminal below shows the output of running the server and the client locally.

![Running the server and the client locally](../img/Running.png)

# Please contribute!

If you find ZIO Insight helpful or have any suggestions, feel free to contribute or raise an issue on our GitHub repository. Happy coding!
