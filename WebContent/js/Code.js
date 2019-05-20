
    function scrivi(valore) {
    var calc = document.getElementById("input_calcolo");
    calc.value += valore;
    }
    function calcola() {
    var calc = document.getElementById("input_calcolo");
    calc.value = eval(calc.value);
    }
    function cancella() {
    var calc = document.getElementById("input_calcolo");
    calc.value ="";
    }

    function send() {
    $.ajax({
            method: "GET",
            url: "http://localhost:8080/project1/Services/operation/Operator",
            dataType: "json",
            success : handleData
          });
        };
        function handleData(data) {
            alert(data);
        }






        $(document).ready(function(){
          const URL = "http://192.168.1.206:11077/ir/Services/reference/status";
          $(".reference1 ").click(function(){
            $.get(URL, function(result){
              console.log(result);
              $.each(result.response.projectID, function (index, value) {
           console.log(this.text); })
            })
        })
