if ($('.file-selector').length) {
  $('.file-selector').each(function() {
    var pF = $(this),
    fileInput = pF.find('input[type="file"]');
    fileInput.hide();
    
    fileInput.change(function() {
      fileName = fileInput.val().split('\\').pop();
      pF.find('input[type="text"]').val(fileName);
    });

    pF.find('button').click(function(e) {
      e.preventDefault();
      fileInput.click();
    })
  });
}