import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import com.typesafe.sbt.web.Import._
import net.ground5hark.sbt.concat.Import._
import com.typesafe.sbt.uglify.Import._
import com.typesafe.sbt.digest.Import._
import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.{SbtArtifactory, SbtAutoBuildPlugin, _}
import DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, integrationTestSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

val appName = "voa-bar-xml-frontend"

lazy val appDependencies : Seq[ModuleID] = Dependencies.appDependencies
lazy val plugins : Seq[Plugins] = Seq.empty
lazy val playSettings : Seq[Setting[_]] = Seq.empty

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(playSettings : _*)
  .settings( majorVersion := 0 )
  .settings(RoutesKeys.routesImport ++= Seq("models._"))
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*identifiers;.*models.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*DataCacheConnector;.*AutobarsInterpreter;.*UniformController;" +
      ".*ControllerConfiguration;.*LanguageSwitchController;.*FormErrorHelper;.*FrontendAppConfig;.*Constraints;" +
      ".*Formatters;.*CheckYourAnswersHelper;.*FormHelpers;.*error_template.template;.*main_template.template;.*pageChrome.template;.*feedbackError.template",
    ScoverageKeys.coverageMinimum := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false,
    PlayKeys.playDefaultPort := 8448
  )
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.11.12",
    scalacOptions += "-Ypartial-unification",
    scalacOptions += "-J-Xss4M",
    libraryDependencies ++= appDependencies,
    retrieveManaged := true,
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    dependencyOverrides := Dependencies.dependencyOverrides
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers ++= Seq(
    Resolver.bintrayRepo("hmrc", "releases"),
    Resolver.jcenterRepo,
    Resolver.bintrayRepo("emueller", "maven")
  ))
  .settings(
    // concatenate js
    Concat.groups := Seq(
      "javascripts/voabarxmlfrontend-app.js" -> group(Seq("javascripts/show-hide-content.js", "javascripts/feedback.js", "javascripts/voabarxmlfrontend.js"))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    pipelineStages in Assets := Seq(concat,uglify),
    // only compress files generated by concat
    includeFilter in uglify := GlobFilter("voabarxmlfrontend-*.js")
  )
