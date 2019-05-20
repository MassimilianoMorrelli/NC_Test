
function test2(element){
var test3 = element.id;
$('#summary').hide();
var rows = $('#summary tr');
$('#summary').row(element.id).show();
}

/*DropDown*/
$(document).ready(function(){
     const URL = "http://192.168.1.206:11077/ir/Services/reference/status";
     $(".dropdown").click(function(){
       $.ajax({
         type: "GET",
         url: URL,
         dataType: "json",
         success:function(result) {
             $.each(result.response,function(i,value)
             {
              var resultant="<li><a>"+value.projectID+"</a></li>";
                 $(resultant).appendTo('.dropdown-menu');
               });
          }});
     });
 });
/*Project Summary*/
function test(event){
  var result = $.parseJSON(event.data);
    $('#interface').empty();
  var table = '';
  $.each(result.scheduling, function (index, value) {
  table += '<tr><th style="text-align:center"><a onclick="test2('+value.projectID+')" class="nav-link active" data-toggle="modal" data-target="#exampleModal" href="" >' + value.projectID +
             '</td><td style="text-align:center">' + value.status +
             '</td><td style="text-align:center">' + value.Start_Scheduler_Datetime +
             '</td><td style="text-align:center">' + value.End_Scheduler_Datetime +
             '</td><td style="text-align:center">' + value.Next_Scheduler +
             '</td><td style="text-align:center">' + value.updateFreq +
             '</td><td class="progress-bar progress-bar-striped " style="width: 100%; vertical-align:middle" aria-valuenow="25" aria-valuemin="0" aria-valuemax="100">' + value.progress +
             '</td><td style="text-align:center"><img src="img/pause.png" class="btn btn-info stop" height="30" alt="" ' + value.updateFreq +
             '</td></tr>';
  });
  $('#interface').append(table);
$('#summary').empty();
  var table_summary = '';
  $.each(result.summary, function (index, value) {
    table_summary += '<tr><td id="'+value.projectID+'">'  + value.projectID +
             '</td><td>' + value.indexID +
             '</td><td>' + value.status +
             '</td><td>' + value.Start_InvertedIndex_Datetime +
             '</td><td>' + value.End_InvertedIndex_Datetime +
             '</td><td>' + value.indexed +
             '</td><td>' + value.Start_Indexing_Datetime +
             '</td><td>' + value.End_Indexing_Datetime +
             '</td><td>' + value.backup_Status +
             '</td><td>' + value.Start_Backup_Datetime +
             '</td><td>' + value.End_Backup_Datetime +
             '</td><td>' + value.backup_Indexed +
             '</td><td>' + value.Start_Backup_Indexed_Datetime +
             '</td><td>' + value.End_Backup_Indexed_Datetime +
             '</td></tr>';
                });

      $('#summary').append(table_summary);


  window.localStorage.setItem('savedTable', table_summary);

  $('#reference').empty();
        var table_reference = '';
        $.each(result.reference, function (index, value) {
          table_reference += '<tr><td>'  + value.projectID +
                   '</td><td>' + value.indexID +
                   '</td></tr>';});
        $('#reference').append(table_reference);

        window.localStorage.setItem('savedTable', table_reference);


 }

<!--***************** -->
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/scheduler/resume/ac3";
      $(".resume").click(function(){
        $.get(URL, function(result){
          $('.ipText').val("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");})
        })
    })
    $(document).ready(function(){
      const URL = "http://192.168.1.206:11077/ir/Services/scheduler/stop/ac3";
      $(".stop").click(function(){
        $.get(URL, function(result){
          console.log(result);
          $('.form-control').val(JSON.stringify(result.message));});
        });
    })
