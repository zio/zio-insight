---
id: how-to-contribute
title:  "How to Contribute?"
---

Thank you for your interest in contributing to ZIO, which is a small, zero-dependency library for doing type-safe, composable concurrent and asynchronous programming!

We welcome contributions from all people! You will learn about functional programming, and you will add your own unique touch to the ZIO project. We are happy to help you to get started and to hear your suggestions and answer your questions.

_You too can contribute to ZIO, we believe in you!_

# Contributing

## Getting Started

To begin contributing, please follow these steps:

### Get The Project

If you don't already have one, sign up for a free [GitHub Account](https://github.com/join?source=header-home).

After you [log into](https://github.com/login) GitHub using your account, go to the [ZIO Insight Project Page](https://github.com/zio/zio-insight), and click on [Fork](https://github.com/zio/zio-insight/fork) to fork the ZIO Insight repository into your own account.

You will make _all_ contributions from your own account. No one contributes _directly_ to the main repository. Contributors only ever merge code from other people's forks into the main repository.

Once you have forked the repository, you can now clone your forked repository to your own machine, so you have a complete copy of the project and can begin safely making your modifications (even without an Internet connection).

To clone your forked repository, first make sure you have installed [Git](https://git-scm.com/downloads), the version control system used by GitHub. Then open a Terminal and type the following commands:

```bash
mkdir zio-insight
cd zio-insight
git clone git@github.com:your-user-name/zio-insight.git .
```

If these steps were successful, then congratulations, you now have a complete copy of the ZIO project!

The next step is to build the project on your machine, to ensure you know how to compile the project and run tests.

### Build the Project

For this project we are exploring [mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) as the primary build tool. Once the project matures, the plan is to support both mill and sbt. 

A mill build file is included in the project, so if you choose to build the project this way, you won't have to do any additional configuration or setup (others choose to build the project using IntelliJ IDEA, Gradle, Maven, SBT, or Fury).

We use a mill wrapper script [millw](https://github.com/lefou/millw) to download the required version of mill upon the first build. We recommend to use that script rather than a locally installed version of mill. This will ensure that the build is using the correct mill version. 

The `millw` script is in the root of the repository. To launch this script and see all currently defined mill targets, 
simply type:

```bash
./millw -i resolve __
```

Mill will launch, read the project build file, and download dependencies as required. Finally, it will show the currently defined targets.

You can now compile the test and production source code with the following command:

```bash
./millw -i -j 0 __.compile 
```

To find the currently defined test targets, you can run 

```bash
./millw -i resolve __.test
```

To run all tests, you can run 

```bash
./millw -i -j 0 __.test
```

While you are working the projects, we recommend to keep the tests continuously running in a separate terminal 
using:

```bash
./millw -i -w -j 0 __.testCached
```

This will keep the mill session running and watch the source directories. It will recompile and retest the code 
that has been affected by the latest changes saved. It will also memorize which tests have failed before and reexecute
those once a source file is saved.

[Learn more](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) about mill to understand how you can list projects, switch projects, and otherwise manage a mill project.

### Find an Issue

You may have your own idea about what contributions to make to ZIO Insight, which is great! If you want to make sure the ZIO contributors are open to your idea, you can [open an issue](https://github.com/zio/zio-insight/issues/new) first on the ZIO project site.

Otherwise, if you have no ideas about what to contribute, you can find a large supply of feature requests and bugs on the project's [issue tracker](https://github.com/zio/zio-insight/issues).

Issues are tagged with various labels, such as `good first issue`, which help you find issues that are a fit for you.

If some issue is confusing or you think you might need help, then just post a comment on the issue asking for help. Typically, the author of the issue will provide as much help as you need, and if the issue is critical, leading ZIO contributors will probably step in to mentor you and give you a hand, making sure you understand the issue thoroughly.

Once you've decided on an issue and understand what is necessary to complete the issue, then it's a good idea to post a comment on the issue saying that you intend to work on it. Otherwise, someone else might work on it too!

### Fix an Issue

Once you have an issue, the next step is to fix the bug or implement the feature. Since ZIO is an open source project, there are no deadlines. Take your time!

The only thing you have to worry about is if you take too long, especially for a critical issue, eventually someone else will come along and work on the issue.

If you shoot for 2-3 weeks for most issues, this should give you plenty of time without having to worry about having your issue stolen.

If you get stuck, please consider [opening a pull request](https://github.com/zio/zio-insight/compare) for your incomplete work, and asking for help (just prefix the pull request by _WIP_). In addition, you can comment on the original issue, pointing people to your own fork. Both of these are great ways to get outside help from people more familiar with the project.

### Prepare Your Code

If you've gotten this far, congratulations! You've implemented a new feature or fixed a bug. Now you're in the last mile, and the next step is submitting your code for review, so that other contributors can spot issues and help improve the quality of the code.

To do this, you need to commit your changes locally. A good way to find out what you did locally is to use the `git status` command:

```bash
git status
```

If you see new files, you will have to tell `git` to add them to the repository using `git add`:

```bash
git add core/src/shared/zio/zio/NewFile.scala
```

Then you can commit all your changes at once with the following command:

```bash
git commit -am "Fixed #94211 - Optimized race for lists of effects"
```

At this point, you have saved your work locally, to your machine, but you still need to push your changes to your fork of the repository. To do that, use the `git push` command:

```bash
git push
```

Now while you were working on this great improvement, it's quite likely that other ZIO contributors were making their own improvements. You need to pull all those improvements into your own code base to resolve any conflicts and make sure the changes all work well together.

To do that, use the `git pull` command:

```bash
git pull git@github.com:zio/zio-insight.git master
```

You may get a warning from Git that some files conflicted. Don't worry! That just means you and another contributor edited the same parts of the same files.

Using a text editor, open up the conflicted files, and try to merge them together, preserving your changes and the other changes (both are important!).

Once you are done, you can commit again:

```bash
git commit -am "merged upstream changes"
```

At this point, you should re-run all tests to make sure everything is passing:

```bash
./millw -i __.test
```

If all the tests are passing, then you can check if your code is formatted correctly:

```bash
./millw -i __.checkFormat
```

In case you see any formatting errors, you can either fix those manually in your IDE or you can run 

```bash
./millw -i mill.scalalib.scalafmt.ScalafmtModule/reformatAll __.sources
```

If your changes altered an API, then you may need to rebuild the micro site to make sure none of the (compiled) documentation breaks. Note that you need to install [NodeJS](https://nodejs.org/en/) to build the documentation:

```bash
./millw -i __.docusaurusBuild
```

Finally, if you are up-to-date with master, all your tests are passing, you have properly formatted your code, and the microsite builds properly, then it's time to submit your work for review!

### Work on the documentation 

Good documentation is a crucial part of every project. In ZIO Insight we are using [Docusaurus 2](https://docusaurus.io) to build the micro site. All the infra structure code is located within the `website` folder. You only need to 
touch the files in that folder in case you are making structural changes to the microsite (for example by adding a 
new section to the navigation bar or by changing the navigation menu on the left hand side of the site)

All content is located in the `docs` folder and this is where you will make most of your changes working on the actual 
documentation. 

ZIO Insight uses additional plugins to generate the web pages as described in this [blog post](https://blended-zio.github.io/blended-zio/blog/doc-helpers/). 

The easiest way to work on the documentation is to run 

```bash
./millw -i __.mdocWatch
```

in one terminal session. This will create an initial build of the microsite in `out/zio/site/docusaurusBuild/dest`. 

Once the initial build has finished, the mill session will start to watch for changes in the `docs` directory. Upon any change 
in that directory, the md files will be recompiled with [mdoc](https://scalameta.org/mdoc/) and the output directory will be updated with the compilation result. 

On another terminal, navigate to `out/zio/site/docusaurusBuild/dest` and run `yarn start`. This will start a docusaurus development server on `localhost:3000`. 
You can open the micro site at http://localhost:3000/zio-insight. Any changes made in the `docs`folder will be reflected immediately in the browser.
### Create a Pull Request

To create a pull request, first push all your changes to your fork of the project repository:

```bash
git push
```

Next, [open a new pull request](https://github.com/zio/zio-insight/compare) on GitHub, and select _Compare Across Forks_. On the right hand side, choose your own fork of the ZIO repository, in which you've been making your contribution.

Provide a description for the pull request, which details the issue it is fixing, and has other information that may be helpful to developers reviewing the pull request.

Finally, click _Create Pull Request_!

### Get Your Pull Request Merged

Once you have a pull request open, it's still your job to get it merged! To get it merged, you need at least one core ZIO contributor to approve the code.

If you know someone who would be qualified to review your code, you can request that person, either in the comments of the pull request, or on the right-hand side, if you have appropriate permissions.

Code reviews can sometimes take a few days, because open source projects are largely done outside of work, in people's leisure time. Be patient, but don't wait forever. If you haven't gotten a review within a few days, then consider gently reminding people that you need a review.

Once you receive a review, you will probably have to go back and make minor changes that improve your contribution and make it follow existing conventions in the code base. This is normal, even for experienced contributors, and the rigorous reviews help ensure ZIO's code base stays high quality.

After you make changes, you may need to remind reviewers to check out the code again. If they give a final approval, it means your code is ready for merge! Usually this will happen at the same time, though for controversial changes, a contributor may wait for someone more senior to merge.

If you don't get a merge in a day after your review is successful, then please gently remind folks that your code is ready to be merged.

Sit back, relax, and enjoy being a ZIO Insight contributor!

# ZIO Contributor License Agreement

Thank you for your interest in contributing to the ZIO open source project.

This contributor agreement ("Agreement") describes the terms and conditions under which you may Submit a Contribution to Us. By Submitting a Contribution to Us, you accept the terms and conditions in the Agreement. If you do not accept the terms and conditions in the Agreement, you must not Submit any Contribution to Us.

This is a legally binding document, so please read it carefully before accepting the terms and conditions. If you accept this Agreement, the then-current version of this Agreement shall apply each time you Submit a Contribution. The Agreement may cover more than one software project managed by Us.

## 1. Definitions

"We" or "Us" means Ziverge, Inc., and its duly appointed and authorized representatives.

"You" means the individual or entity who Submits a Contribution to Us.

"Contribution" means any work of authorship that is Submitted by You to Us in which You own or assert ownership of the Copyright. You may not Submit a Contribution if you do not own the Copyright in the entire work of authorship.

"Copyright" means all rights protecting works of authorship owned or controlled by You, including copyright, moral and neighboring rights, as appropriate, for the full term of their existence including any extensions by You.

"Material" means the work of authorship which is made available by Us to third parties. When this Agreement covers more than one software project, the Material means the work of authorship to which the Contribution was Submitted. After You Submit the Contribution, it may be included in the Material.

"Submit" means any form of electronic, verbal, or written communication sent to Us or our representatives, including but not limited to electronic mailing lists, electronic mail, source code control systems, pull requests, and issue tracking systems that are managed by, or on behalf of, Us for the purpose of discussing and improving the Material, but excluding communication that is conspicuously marked or otherwise designated in writing by You as "Not a Contribution."

"Submission Date" means the date on which You Submit a Contribution to Us.

"Effective Date" means the earliest date You execute this Agreement by Submitting a Contribution to Us.

## 2. Grant of Rights

### 2.1 Copyright License

2.1.1. You retain ownership of the Copyright in Your Contribution and have the same rights to use or license the Contribution which You would have had without entering into the Agreement.

2.1.2. To the maximum extent permitted by the relevant law, You grant to Us a perpetual, worldwide, non-exclusive, transferable, royalty-free, irrevocable license under the Copyright covering the Contribution, with the right to sublicense such rights through multiple tiers of sublicensees, to reproduce, modify, display, perform and distribute the Contribution as part of the Material; provided that this license is conditioned upon compliance with Section 2.3.

### 2.2 Patent License

For patent claims including, without limitation, method, process, and apparatus claims which You own, control or have the right to grant, now or in the future, You grant to Us a perpetual, worldwide, non-exclusive, transferable, royalty-free, irrevocable patent license, with the right to sublicense these rights to multiple tiers of sublicensees, to make, have made, use, sell, offer for sale, import and otherwise transfer the Contribution and the Contribution in combination with the Material (and portions of such combination). This license is granted only to the extent that the exercise of the licensed rights infringes such patent claims; and provided that this license is conditioned upon compliance with Section 2.3.

### 2.3 Outbound License

Based on the grant of rights in Sections 2.1 and 2.2, if We include Your Contribution in a Material, We may license the Contribution under any license, including copyleft, permissive, commercial, or proprietary licenses. As a condition on the exercise of this right, We agree to also license the Contribution under the terms of the license or licenses which We are using for the Material on the Submission Date.

### 2.4 Moral Rights

If moral rights apply to the Contribution, to the maximum extent permitted by law, You waive and agree not to assert such moral rights against Us or our successors in interest, or any of our licensees, either direct or indirect.

### 2.5 Our Rights

You acknowledge that We are not obligated to use Your Contribution as part of the Material and may decide to include any Contribution We consider appropriate.

### 2.6 Reservation of Rights

Any rights not expressly licensed under this section are expressly reserved by You.

## 3. Agreement

You confirm that:

a. You have the legal authority to enter into this Agreement.

b. You own the Copyright and patent claims covering the Contribution which are required to grant the rights under Section 2.

c. The grant of rights under Section 2 does not violate any grant of rights which You have made to third parties, including Your employer. If You are an employee, You have had Your employer approve this Agreement or sign the Entity version of this document. If You are less than eighteen years old, please have Your parents or guardian sign the Agreement.

d. You have followed the instructions in, if You do not own the Copyright in the entire work of authorship Submitted.

## 4. Disclaimer

EXCEPT FOR THE EXPRESS WARRANTIES IN SECTION 3, THE CONTRIBUTION IS PROVIDED "AS IS". MORE PARTICULARLY, ALL EXPRESS OR IMPLIED WARRANTIES INCLUDING, WITHOUT LIMITATION, ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT ARE EXPRESSLY DISCLAIMED BY YOU TO US. TO THE EXTENT THAT ANY SUCH WARRANTIES CANNOT BE DISCLAIMED, SUCH WARRANTY IS LIMITED IN DURATION TO THE MINIMUM PERIOD PERMITTED BY LAW.

## 5. Consequential Damage Waiver

TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, IN NO EVENT WILL YOU BE LIABLE FOR ANY LOSS OF PROFITS, LOSS OF ANTICIPATED SAVINGS, LOSS OF DATA, INDIRECT, SPECIAL, INCIDENTAL, CONSEQUENTIAL AND EXEMPLARY DAMAGES ARISING OUT OF THIS AGREEMENT REGARDLESS OF THE LEGAL OR EQUITABLE THEORY (CONTRACT, TORT OR OTHERWISE) UPON WHICH THE CLAIM IS BASED.

## 6. Miscellaneous

6.1. This Agreement will be governed by and construed in accordance with the laws of the state of Maryland, in the United States of America, excluding its conflicts of law provisions. Under certain circumstances, the governing law in this section might be superseded by the United Nations Convention on Contracts for the International Sale of Goods ("UN Convention") and the parties intend to avoid the application of the UN Convention to this Agreement and, thus, exclude the application of the UN Convention in its entirety to this Agreement.

6.2. This Agreement sets out the entire agreement between You and Us for Your Contributions to Us and overrides all other agreements or understandings.

6.3. If You or We assign the rights or obligations received through this Agreement to a third party, as a condition of the assignment, that third party must agree in writing to abide by all the rights and obligations in the Agreement.

6.4. The failure of either party to require performance by the other party of any provision of this Agreement in one situation shall not affect the right of a party to require such performance at any time in the future. A waiver of performance under a provision in one situation shall not be considered a waiver of the performance of the provision in the future or a waiver of the provision in its entirety.

6.5. If any provision of this Agreement is found void and unenforceable, such provision will be replaced to the extent possible with a provision that comes closest to the meaning of the original provision and which is enforceable. The terms and conditions set forth in this Agreement shall apply notwithstanding any failure of essential purpose of this Agreement or any limited remedy to the maximum extent possible under law.
