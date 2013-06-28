// Inspired from
// http://blog.anorgan.com/2012/09/30/pretty-multi-file-upload-bootstrap-jquery-twig-silex/
// and http://jasny.github.io/bootstrap/javascript.html#fileupload
if ($('.file-selector').length) {
  $('.file-selector').each(function() {
    button = $(this).find('button');
    text = $(this).find('input[type="text"]');
    fileInput = $(this).find('input[type="file"]');
    fileInput.hide();
    
    fileInput.change(function() {
      text.val(fileInput.val().split('\\').pop());
    });

    button.click(function(e) {
      e.preventDefault();
      fileInput.click();
    })
  });
}