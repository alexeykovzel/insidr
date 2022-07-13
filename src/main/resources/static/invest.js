function getEdgarIds() {
    $.ajax({
        type: "GET",
        url: "http://localhost:8080/edgar/ids",
        success: (data) => alert("DATA: " + data),
        error: (e) => alert(e.responseText),
    });
}