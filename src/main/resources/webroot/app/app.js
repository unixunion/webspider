 $(document).ready(function(){

    // connect to eventbus
    initialiseEventbus = function() {
        console.log("Checking Connection");

        if (eb != null && eb.readyState() == 1) {
            clearInterval(reconnector);
            return;
        };

        console.log("New Connection");
        eb = new vertx.EventBus("/eventbus/", vertxbus_ping_interval = 1000);

        eb.onopen = function () {
            console.log("Registering topics");
            clearInterval(reconnector);
            // subscribe to broadcast messages from the server
            eb.registerHandler("broadcast", function (msg) {
                console.log(msg);
            });

            msg = {};
            msg['action'] = 'cache-purge-all'

            eb.send("DxTemplateEngineImpl", msg, function(msg) {
                console.log(msg);
            });
        }

        eb.onclose = function () {
            console.log("Disconnected");
            clearInterval(reconnector);
            reconnector = setInterval(initialiseEventbus, 5000);
        }
    }

     // call first time connect
    initialiseEventbus();

});