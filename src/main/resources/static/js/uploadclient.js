document.addEventListener("DOMContentLoaded", function () {
    var form = document.getElementById("form");
    form.action = location.protocol + "//" + location.hostname + ":" + 8081 + "/images";
});