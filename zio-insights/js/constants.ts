const scalaVersion = "2.13";

const prodModeTag = "production";
const projectName = "zio-insights";
const projectRootPath = ".";

const isInProdMode = process.env.NODE_ENV === prodModeTag;
const sjsMode = isInProdMode ? "opt" : "fastopt";
const cssPath = `${projectRootPath}/src/main/static/css`;
const projectTargetPath = `${projectRootPath}/target`;
const sjsGenPath = `${projectTargetPath}/scala-${scalaVersion}/${projectName}-${sjsMode}`;

module.exports = {
  cssPath,
  isInProdMode,
  prodModeTag,
  projectName,
  projectRootPath,
  projectTargetPath,
  scalaVersion,
  sjsMode,
  sjsGenPath,
};
