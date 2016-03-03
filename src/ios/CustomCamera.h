//
//  CustomCamera.h
//  CustomCamera
//
//  Created by Jin on 24/08/2014.
//
//


#import <Cordova/CDV.h>

@interface CustomCamera : CDVPlugin<UIImagePickerControllerDelegate, UINavigationControllerDelegate>
{
    CDVInvokedUrlCommand *lastCommand;
    
    NSString *filename;
    CGFloat quality;
    CGFloat targetWidth;
    CGFloat targetHeight;
    
    int nDestType;
    int nSourceType;
    
}
- (void)takePicture:(CDVInvokedUrlCommand*)command;

@end
