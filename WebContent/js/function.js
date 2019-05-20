    $(function () {
      $('[data-toggle="tooltip"]').tooltip()
    })

    function unlock(el1, el2) {
       if(el1.checked) {
         document.getElementById(el2).disabled = false;
       } else {
         document.getElementById(el2).disabled = 'disabled';
       }
     }

     function toggleText() {
   var Disabled = document.getElementById("txtBox").disabled;
   document.getElementById("txtBox").disabled = !Disabled;
   }

   $(document).ready(function(){
       $('#data').after('<div id="nav"></div>');
       var rowsShown = 4;
       var rowsTotal = $('#data tbody tr').length;
       var numPages = rowsTotal/rowsShown;
       for(i = 0;i < numPages;i++) {
           var pageNum = i + 1;
           $('#nav').append('<a href="#" rel="'+i+'">'+pageNum+'</a> ');
       }
       $('#data tbody tr').hide();
       $('#data tbody tr').slice(0, rowsShown).show();
       $('#nav a:first').addClass('active');
       $('#nav a').bind('click', function(){
           $('#nav a').removeClass('active');
           $(this).addClass('active');
           var currPage = $(this).attr('rel');
           var startItem = currPage * rowsShown;
           var endItem = startItem + rowsShown;
           $('#data tbody tr').css('opacity','0.0').hide().slice(startItem, endItem).
           css('display','table-row').animate({opacity:1}, 300);
       });
   });
