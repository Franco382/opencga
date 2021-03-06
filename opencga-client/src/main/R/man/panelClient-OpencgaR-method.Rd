% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/Panel-methods.R
\name{panelClient,OpencgaR-method}
\alias{panelClient,OpencgaR-method}
\title{PanelClient methods}
\usage{
\S4method{panelClient}{OpencgaR}(OpencgaR, panels, members, endpointName, params = NULL, ...)
}
\arguments{
\item{panels}{Comma separated list of panel ids.}

\item{members}{Comma separated list of user or group ids.}

\item{action}{Action to be performed \link{ADD, SET, REMOVE or RESET}.}

\item{source}{Comma separated list of sources to import panels from. Current supported sources are 'panelapp' and 'genecensus'.}

\item{id}{Comma separated list of panel IDs to be imported from the defined source.If 'source' is provided and 'id' is empty, it will import all the panels from the source. When 'id' is provided, only one 'source' will be allowed.}

\item{limit}{Number of results to be returned.}

\item{skip}{Number of results to skip.}

\item{count}{Get the total number of results matching the query. Deactivated by default.}

\item{name}{Panel name.}

\item{phenotypes}{Panel phenotypes.}

\item{variants}{Panel variants.}

\item{genes}{Panel genes.}

\item{regions}{Panel regions.}

\item{categories}{Panel categories.}

\item{tags}{Panel tags.}

\item{description}{Panel description.}

\item{author}{Panel author.}

\item{creationDate}{Creation date. Format: yyyyMMddHHmmss. Examples: >2018, 2017-2018, <201805.}

\item{modificationDate}{Modification date. Format: yyyyMMddHHmmss. Examples: >2018, 2017-2018, <201805.}

\item{acl}{Filter entries for which a user has the provided permissions. Format: acl={user}:{permissions}. Example: acl=john:WRITE,WRITE_ANNOTATIONS will return all entries for which user john has both WRITE and WRITE_ANNOTATIONS permissions. Only study owners or administrators can query by this field. .}

\item{release}{Release value (Current release from the moment the samples were first created).}

\item{snapshot}{Snapshot value (Latest version of samples in the specified release).}

\item{member}{User or group id.}

\item{silent}{Boolean to retrieve all possible entries that are queried for, false to raise an exception whenever one of the entries looked for cannot be shown for whichever reason.}

\item{include}{Fields included in the response, whole JSON path must be provided.}

\item{exclude}{Fields excluded in the response, whole JSON path must be provided.}

\item{version}{Panel  version.}

\item{deleted}{Boolean to retrieve deleted panels.}

\item{study}{Study [\link{user@}project:]study where study and project can be either the ID or UUID.}

\item{incVersion}{Create a new version of panel.}

\item{data}{Panel parameters.}
}
\description{
This function implements the OpenCGA calls for managing Disease Panels.
The following table summarises the available \emph{actions} for this client:\tabular{llr}{
   endpointName \tab Endpoint WS \tab parameters accepted \cr
   updateAcl \tab /{apiVersion}/panels/acl/{members}/update \tab study, members\link{*}, action\link{*}, body\link{*} \cr
   create \tab /{apiVersion}/panels/create \tab study, source, id, body \cr
   search \tab /{apiVersion}/panels/search \tab include, exclude, limit, skip, count, study, name, phenotypes, variants, genes, regions, categories, tags, description, author, deleted, creationDate, modificationDate, acl, release, snapshot \cr
   acl \tab /{apiVersion}/panels/{panels}/acl \tab panels\link{*}, study, member, silent \cr
   delete \tab /{apiVersion}/panels/{panels}/delete \tab study, panels \cr
   info \tab /{apiVersion}/panels/{panels}/info \tab include, exclude, panels, study, version, deleted \cr
   update \tab /{apiVersion}/panels/{panels}/update \tab study, panels, incVersion, body \cr
}
}
\section{Endpoint /{apiVersion}/panels/acl/{members}/update}{

Update the set of permissions granted for the member.
}

\section{Endpoint /{apiVersion}/panels/create}{

Create a panel.
}

\section{Endpoint /{apiVersion}/panels/search}{

Panel search.
}

\section{Endpoint /{apiVersion}/panels/{panels}/acl}{

Returns the acl of the panels. If member is provided, it will only return the acl for the member.
}

\section{Endpoint /{apiVersion}/panels/{panels}/delete}{

Delete existing panels.
}

\section{Endpoint /{apiVersion}/panels/{panels}/info}{

Panel info.
}

\section{Endpoint /{apiVersion}/panels/{panels}/update}{

Update panel attributes.
}

\seealso{
\url{http://docs.opencb.org/display/opencga/Using+OpenCGA} and the RESTful API documentation
\url{http://bioinfo.hpc.cam.ac.uk/opencga-prod/webservices/}
\link{*}: Required parameter
}
