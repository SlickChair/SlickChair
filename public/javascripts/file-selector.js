// Inspired from
// http://blog.anorgan.com/2012/09/30/pretty-multi-file-upload-bootstrap-jquery-twig-silex/
if ($('.file-selector').length) {
  $('.file-selector').each(function() {
    var selectorDiv = $(this),
    fileInput = selectorDiv.find('input[type="file"]');
    fileInput.hide();
    
    fileInput.change(function() {
      fileName = fileInput.val().split('\\').pop();
      selectorDiv.find('input[type="text"]').val(fileName);
    });

    selectorDiv.find('button').click(function(e) {
      e.preventDefault();
      fileInput.click();
    })
  });
}