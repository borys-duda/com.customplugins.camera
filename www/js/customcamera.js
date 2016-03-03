var customCamera = {
    getPicture: function(success, failure, options) {
        options = options || {};
        var quality = options.quality || 100;
        var targetWidth = options.targetWidth || -1;
        var targetHeight = options.targetHeight || -1;
        var destinationType = options.destinationType || 0;
        var pictureSourceType = options.pictureSourceType || 0;
        var direction = options.direction || 0;
        cordova.exec(success, failure, "CustomCamera", "takePicture", ['photo.jpg', quality, targetWidth, targetHeight, destinationType, pictureSourceType, direction]);
    }
};

customCamera.DestinationType = {
    DATA_URL : 0,      // Return image as base64-encoded string
    FILE_URI : 1,      // Return image file URI
};

customCamera.PictureSourceType = {
    PHOTOLIBRARY : 0,
    CAMERA : 1,
};

customCamera.Direction = {
    BACK : 0,      // Use the back-facing camera
    FRONT : 1      // Use the front-facing camera
};

module.exports = customCamera;
