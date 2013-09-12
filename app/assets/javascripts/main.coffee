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
  nauthors = $("#nauthors")
  getNauthors = -> Number(nauthors.val())
  setNauthors = (n) ->
    oldn = getNauthors()
    nauthors.val(n)
    if nauthors.val() == null
      nauthors.val(oldn)
      false
    else
      true
  incNauthors = -> setNauthors(getNauthors() + 1)
  decNauthors = -> setNauthors(getNauthors() - 1)
  
  disableMinusIfZero = ->
    if getNauthors() == 1
      $(".glyphicon-minus-sign").addClass("half-transparent")
    else
      $(".glyphicon-minus-sign").removeClass("half-transparent")
  disableMinusIfZero()
  
  $(".glyphicon-plus-sign").click (e) ->
    if incNauthors()
      $("#author#{ getNauthors() - 1 }").show()
    disableMinusIfZero()
    
  $(".glyphicon-minus-sign").click (e) ->
    if decNauthors()
      $("#author#{ getNauthors() }").hide()
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
