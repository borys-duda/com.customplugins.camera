//
//  CustomCameraViewController.h
//  CustomCamera
//
//  Created by Jin on 24/08/2014.
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@class AVCamCaptureManager, AVCamPreviewView, AVCaptureVideoPreviewLayer;

@interface CustomCameraViewController : UIViewController
{
    NSData *_imageData;
}

- (id)initWithCallback:(void(^)(UIImage*))callback;

@property (nonatomic,strong) AVCamCaptureManager *captureManager;
@property (nonatomic,strong) IBOutlet UIView *videoPreviewView;
@property (nonatomic,strong) AVCaptureVideoPreviewLayer *captureVideoPreviewLayer;

@end
