$ ->
  # Bootstrap file selector. Inspired from http://blog.anorgan.com/2012/09/30
  # /pretty-multi-file-upload-bootstrap-jquery-twig-silex/ and
  # http://jasny.github.io/bootstrap/javascript.html#fileupload
  if $(".file-selector").length
    $(".file-selector").each ->
      button = $(this).find("button")
      text = $(this).find(":text")
      fileInput = $(this).find(":file")
      fileInput.change ->
        text.val(fileInput.val().split("\\").pop())
      button.click (e) ->
        e.preventDefault()
        fileInput.click()

  # Hides/show javascript managed elements.
  $(".hidejs").each -> $(this).hide()
  $(".showjs").each -> $(this).show()
  $("#author0").show()
  
  # Handles plus and minus buttons.
  minus = $(".glyphicon-minus-sign")
  plus = $(".glyphicon-plus-sign")
  nauthors = $("#nauthors")
  disableMinusIfZero = ->
    minus.css("color", if Number(nauthors.val()) == 1 then "#a6a6a6" else plus.css("color"))
  disableMinusIfZero()
  
  plus.click (e) ->
    n = Number(nauthors.val())
    if nauthors.val(n + 1).val() == null
      nauthors.val(n)
    else
      $("#author#{ n }").show()
    disableMinusIfZero()
    
  minus.click (e) ->
    n = Number(nauthors.val())
    if nauthors.val(n - 1).val() == null
      nauthors.val(n)
    else
      $("#author#{ n - 1 }").hide()
    disableMinusIfZero()
  
  # /slq, select the query textarea and submit on ctrl+enter.
  $("#query").select()
  $("body").keydown (e) ->
    ENTER = 13
    if e.keyCode == ENTER and e.ctrlKey
      $(".ctrl-enter").find("button").click()
  
  # Modal login magic.
  $(".email").click (e) ->
    $(".email-form").toggle()
    $(".facebook").toggleClass("half-transparent")
    $(".google").toggleClass("half-transparent")
  
  $("#signup").on "change", (e) ->
    $("#password").val("")
    $("#password").prop("disabled", not $("#password").prop("disabled"))
  
  
