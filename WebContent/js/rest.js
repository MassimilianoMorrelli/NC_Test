<!-- GET -->
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/scheduler/status";
      $(".btnB").click(function(){
        $.ajax ({
          url:URL,
          type: "GET",
          success:function(result){
            console.log(result)}
          })
        })
        })
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/status/project/ac3";
      $(".btnB").click(function(){
        $.ajax ({
          url:URL,
          type: "GET",
          success:function(result){
            console.log(result)}
          })
        })
        })
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/scheduler/resume/ac3";
      $(".resume").click(function(){
        $.ajax ({
          url:URL,
          type: "GET",
          success:function(result){
            console.log(result)}
          })
        })
        })
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/reference/status";
      $(".reference").click(function(){
        $.ajax ({
          url:URL,
          type: "GET",
          success:function(result){
            console.log(result)}
          })
        })
        })
        $(document).ready(function(){
          const URL = "http://192.168.1.206:11077/ir/Services/scheduler/stop/ac3";
          $(".stop").click(function(){
            $.ajax ({
              url:URL,
              type: "GET",
              success:function(result){
                console.log(result)}
              })
            })
            })
<!-- POST -->
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/scheduler/start";
      $(".start").click(function(){
        $.ajax ({
          headers: {},
          url:URL,
          type: "POST",
          data : {
	                 "projectID":"ac3",
	                 "minFrequency":0,
	                 "rebuild":true
                 },
          contentType: "application/json; charset=utf-8",
          dataType: "json",
          success:function(result){
            console.log(result)}
          })
        })
        })
