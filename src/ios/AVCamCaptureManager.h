#import <AVFoundation/AVFoundation.h>

@protocol AVCamCaptureManagerDelegate;

@interface AVCamCaptureManager : NSObject

@property (nonatomic,strong) AVCaptureSession *session;
@property (nonatomic,assign) AVCaptureVideoOrientation orientation;
@property (nonatomic,strong) AVCaptureDeviceInput *videoInput;
@property (nonatomic,strong) AVCaptureDeviceInput *audioInput;
@property (nonatomic,strong) AVCaptureStillImageOutput *stillImageOutput;
@property (nonatomic,weak) id deviceConnectedObserver;
@property (nonatomic,weak) id deviceDisconnectedObserver;
@property (nonatomic,assign) UIBackgroundTaskIdentifier backgroundRecordingID;
@property (nonatomic,weak) id <AVCamCaptureManagerDelegate> delegate;

- (BOOL) setupSession;
- (void) captureStillImage;
- (void) takePictureWaitingForCameraToFocus;
- (BOOL) toggleCamera;
- (NSUInteger) cameraCount;
- (void) autoFocusAtPoint:(CGPoint)point;
- (void) continuousFocusAtPoint:(CGPoint)point;

@end

// These delegate methods can be called on any arbitrary thread. If the delegate does something with the UI when called, make sure to send it to the main thread.
@protocol AVCamCaptureManagerDelegate <NSObject>
@optional
- (void) captureManager:(AVCamCaptureManager *)captureManager didFailWithError:(NSError *)error;
- (void) captureManagerStillImageCaptured:(NSData *)imageData;
- (void) captureManagerDeviceConfigurationChanged:(AVCamCaptureManager *)captureManager;
@end
