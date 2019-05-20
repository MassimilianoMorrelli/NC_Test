<!--POST-->
  $(document).ready(function(){
    const URL = "http://192.168.1.206:11077/ir/Services/scheduler/start";
    $(".POST").click(function(){
      $.ajax({
              url: URL,
              type: "POST",
              data: {
	                   "projectID":"ac3",
	                   "minFrequency":0,
	                   "rebuild":true
                   },
              contentType: "application/json",
              dataType: "json",
                  success: function(result) {
                    $('.form-control').val(JSON.stringify(result.response));}
                  })
            });
})
