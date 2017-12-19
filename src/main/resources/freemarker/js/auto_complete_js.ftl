    <script>
      $(function() {
        $.getJSON("/getState", function(data){
            $( "#state" ).autocomplete({
              minLength: 1,
              source: data,
              focus: function( event, ui ) {
                $( "#state" ).val( ui.item.stateShort );
                return false;
              },
              select: function( event, ui ) {
                $( "#state" ).val( ui.item.stateShort );
                return false;
              }
            })
            .autocomplete( "instance" )._renderItem = function( ul, item ) {
              return $( "<li>" )
                .append( "<a>" + item.value + "<br>" + item.stateShort + "</a>" )
                .appendTo( ul );
            };
          });

        });
  </script>