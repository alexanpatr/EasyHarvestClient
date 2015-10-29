# EasyHarvest
EasyHarvest aims to simplify the deployment and controlled execution of large-scale sensing applications on smartphones. On the one hand, application owners submit to a server sensing tasks for distribution on smartphones, and collect the data produced by them in a simple manner. On the other hand, smartphone owners control the execution of sensing tasks on their devices through a single interface, without having to repeatedly download, install and configure individual sensing applications. The interaction between the smartphone and the server occurs in a transparent way, with tolerance to intermittent connectivity and support for disconnected operation.

*For more information read* [here](http://www.inf.uth.gr/wp-content/uploads/formidable/Katsomallos_Emmanouil.pdf) *(slightly outdated) and* [here](http://www.inf.uth.gr/wp-content/uploads/formidable/Katsomallos_Emmanouil1.pdf).

## Initial Setup

### Paths and Variables
com.www&#8203;.client > Globals.java

| Parameter     | Value         |
| ------------- | ------------- |
| server_url    | The localhost url with the apache port and the application name (Server) providing the REST API. |
| task_latency  | The client polling interval in ms (X * 100 * 1000). |
