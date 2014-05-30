$ ->
  ### Bootstrap file selector. Inspired by
      http://blog.anorgan.com/2012/09/30/pretty-multi-file-upload-bootstrap-jquery-twig-silex/
      and http://jasny.github.io/bootstrap/javascript.html#fileupload
  ###
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

  ### Handles plus and minus buttons ###
  nAuthors = $("#paper_nAuthors")
  getnAuthors = -> Number(nAuthors.val())
  setnAuthors = (n) ->
    oldn = getnAuthors()
    nAuthors.val(n)
    if nAuthors.val() == null
      nAuthors.val(oldn)
      false
    else
      true
  
  # Restores state from the value of nAuthors
  for i in [0..getnAuthors()-1]
    $("#author#{ i }").removeClass("hidden")
  
  disableMinusIfZero = ->
    if getnAuthors() == 1
      $(".glyphicon-minus-sign").addClass("half-transparent")
    else
      $(".glyphicon-minus-sign").removeClass("half-transparent")
  disableMinusIfZero()
  
  $(".glyphicon-plus-sign").click (e) ->
    if setnAuthors(getnAuthors() + 1)
      $("#author#{ getnAuthors() - 1 }").removeClass("hidden")
    disableMinusIfZero()
  
  $(".glyphicon-minus-sign").click (e) ->
    if setnAuthors(getnAuthors() - 1)
      $("#author#{ getnAuthors() }").addClass("hidden")
    disableMinusIfZero()
  
  ### /slq, select the query textarea and submit on ctrl+enter ###
  $("#query").select()
  $("body").keydown (e) ->
    ENTER = 13
    if e.keyCode == ENTER and e.ctrlKey
      $(".ctrl-enter").find("button").click()
  
  # Prevent form submit with enter.
  $(document).keydown (e) ->
    if e.which == 13 && e.target.nodeName != "TEXTAREA"
      false

  ### Login form magic ###
  $(".email").click (e) ->
    e.preventDefault()
    $(".email-form").toggleClass("hidden")
    $(".note").toggleClass("hidden")
    $(".facebook").toggleClass("half-transparent")
    $(".google").toggleClass("half-transparent")
  
  $("#create").prop("checked", false)
  $("#create").on "change", (e) ->
    $("#password").val("")
    $("#password").prop("disabled", not $("#password").prop("disabled"))
  
  # Review comment toggle magic
  $(".toggle-edit").click (e) ->
    e.preventDefault()
    id = e.target.getAttribute("toggle")
    $(".toggle" + id).toggleClass("hidden")

  $('form').areYouSure()
