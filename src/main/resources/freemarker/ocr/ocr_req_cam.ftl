<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title></title>
</head>
<body>


<form action="/ocr/" method="post" enctype="multipart/form-data">
    Select image to upload:
    <input type="file" name="fileToUpload" id="fileToUpload" accept="image/*" capture="camera">
    <input type="submit" value="Upload Image" name="submit">
</form>

</body>
</html>