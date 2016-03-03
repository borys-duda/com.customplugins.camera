//
//  CustomCameraViewController.m
//  CustomCamera
//
//  Created by Jin on 24/08/2014.
//
//


#import "CustomCameraViewController.h"

#import <Cordova/CDV.h>

#import "AVCamCaptureManager.h"

@interface CustomCameraViewController (AVCamCaptureManagerDelegate) <AVCamCaptureManagerDelegate>
@end


@implementation CustomCameraViewController {
    void(^_callback)(UIImage*);

    UIView *_previewButtonPanel;
    UIView *_buttonPanel;
    UIButton *_captureButton;
    UIButton *_backButton;
    UIButton *_retakeButton;
    UIButton *_useButton;
    UIImageView *_topLeftGuide;
    UIImageView *_topRightGuide;
//    UIImageView *_bottomLeftGuide;
//    UIImageView *_bottomRightGuide;
    UIView *_previewView;
    UIImageView *_imagePreview;
    UIActivityIndicatorView *_activityIndicator;
}

static const CGFloat kCaptureButtonWidthPhone = 64;
static const CGFloat kCaptureButtonHeightPhone = 64;
static const CGFloat kBackButtonWidthPhone = 100;
static const CGFloat kBackButtonHeightPhone = 40;
static const CGFloat kUseButtonWidthPhone = 50;
static const CGFloat kBorderImageWidthPhone = 50;
static const CGFloat kBorderImageHeightPhone = 150;
static const CGFloat kHorizontalInsetPhone = 45;
static const CGFloat kVerticalInsetPhone = 25;
static const CGFloat kHorizontalInsetPhone4s = 55;
static const CGFloat kVerticalInsetPhone4s = 20;
static const CGFloat kCaptureButtonVerticalInsetPhone = 10;

static const CGFloat kCaptureButtonWidthTablet = 75;
static const CGFloat kCaptureButtonHeightTablet = 75;
static const CGFloat kBackButtonWidthTablet = 150;
static const CGFloat kBackButtonHeightTablet = 50;
static const CGFloat kUseButtonWidthTablet = 75;
static const CGFloat kBorderImageWidthTablet = 50;
static const CGFloat kBorderImageHeightTablet = 150;
static const CGFloat kHorizontalInsetTablet = 200;
static const CGFloat kVerticalInsetTablet = 50;
static const CGFloat kCaptureButtonVerticalInsetTablet = 20;

static const CGFloat kAspectRatio = 125.0f / 86;

- (id)initWithCallback:(void(^)(UIImage*))callback {
    self = [super initWithNibName:nil bundle:nil];
    if (self) {
        _callback = callback;
    }
    return self;
}

- (void)dealloc {
    
}

- (void)loadView {
    self.view = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    _previewView = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    _imagePreview = [[UIImageView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
    if ([self captureManager] == nil) {
		AVCamCaptureManager *manager = [[AVCamCaptureManager alloc] init];
		[self setCaptureManager:manager];
		
		[[self captureManager] setDelegate:self];
        
		if ([[self captureManager] setupSession]) {
            // Create video preview layer and add it to the UI
			AVCaptureVideoPreviewLayer *newCaptureVideoPreviewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:[[self captureManager] session]];
            
			CALayer *viewLayer = [self.view layer];
			[viewLayer setMasksToBounds:YES];
			
			CGRect bounds = [self.view bounds];
			[newCaptureVideoPreviewLayer setFrame:bounds];
            
			[newCaptureVideoPreviewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
			
			[viewLayer insertSublayer:newCaptureVideoPreviewLayer below:[viewLayer sublayers][0]];
			
			[self setCaptureVideoPreviewLayer:newCaptureVideoPreviewLayer];
			
            // Start the session. This is done asychronously since -startRunning doesn't return until the session is running.
			dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
				[[[self captureManager] session] startRunning];
			});
		}
	}
    
    [NSTimer scheduledTimerWithTimeInterval:0.7 target:self selector:@selector(addOverlay) userInfo:nil repeats:NO];
    [_previewView addSubview:_imagePreview];
    [_previewView addSubview:[self createPreviewOverlay]];
//    [_activityIndicator startAnimating];
}

- (void) addOverlay
{
    [self.view addSubview:[self createOverlay]];
}

- (void)captureStillImage
{
    _captureButton.userInteractionEnabled = NO;
//    _captureButton.selected = YES;
    // Capture a still image
    [[self captureManager] takePictureWaitingForCameraToFocus];
}

- (void)captureManagerStillImageCaptured:(NSData *)imageData
{
    _imageData = imageData;
    _captureButton.userInteractionEnabled = YES;
//    _captureButton.selected = NO;
    CFRunLoopPerformBlock(CFRunLoopGetMain(), kCFRunLoopCommonModes, ^(void) {
        [self preview];
    });
}

- (UIView*)createOverlay {
    UIView *overlay = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
    _buttonPanel = [[UIView alloc] initWithFrame:CGRectZero];
    [_buttonPanel setBackgroundColor:[UIColor colorWithWhite:0 alpha:0.75f]];
    [overlay addSubview:_buttonPanel];
    
    _captureButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_captureButton setImage:[UIImage imageNamed:@"www/img/cameraoverlay/capture_button.png"] forState:UIControlStateNormal];
    [_captureButton setImage:[UIImage imageNamed:@"www/img/cameraoverlay/capture_button_pressed.png"] forState:UIControlStateSelected];
    [_captureButton setImage:[UIImage imageNamed:@"www/img/cameraoverlay/capture_button_pressed.png"] forState:UIControlStateHighlighted];
    [_captureButton addTarget:self action:@selector(captureStillImage) forControlEvents:UIControlEventTouchUpInside];
    [overlay addSubview:_captureButton];
    
    _backButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_backButton setBackgroundImage:[UIImage imageNamed:@"www/img/cameraoverlay/back_button.png"] forState:UIControlStateNormal];
    [_backButton setBackgroundImage:[UIImage imageNamed:@"www/img/cameraoverlay/back_button_pressed.png"] forState:UIControlStateHighlighted];
    [_backButton setTitle:@"Cancel" forState:UIControlStateNormal];
    [_backButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [[_backButton titleLabel] setFont:[UIFont systemFontOfSize:18]];
    [_backButton addTarget:self action:@selector(dismissCameraPreview) forControlEvents:UIControlEventTouchUpInside];
    [overlay addSubview:_backButton];
    
    _topLeftGuide = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"www/img/cameraoverlay/border_top_left.png"]];
    [overlay addSubview:_topLeftGuide];
    
    _topRightGuide = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"www/img/cameraoverlay/border_top_right.png"]];
    [overlay addSubview:_topRightGuide];
    
//    _bottomLeftGuide = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"www/img/cameraoverlay/border_bottom_left.png"]];
//    [overlay addSubview:_bottomLeftGuide];
    
//    _bottomRightGuide = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"www/img/cameraoverlay/border_bottom_right.png"]];
//    [overlay addSubview:_bottomRightGuide];

    return overlay;
}

- (UIView*)createPreviewOverlay {
    UIView *previewoverlay = [[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    
    _previewButtonPanel = [[UIView alloc] initWithFrame:CGRectZero];
    [_previewButtonPanel setBackgroundColor:[UIColor colorWithWhite:0 alpha:0.75f]];
    [previewoverlay addSubview:_previewButtonPanel];
    
    _retakeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_retakeButton setBackgroundImage:[UIImage imageNamed:@"www/img/cameraoverlay/back_button.png"] forState:UIControlStateNormal];
    [_retakeButton setBackgroundImage:[UIImage imageNamed:@"www/img/cameraoverlay/back_button_pressed.png"] forState:UIControlStateHighlighted];
    [_retakeButton setTitle:@"Retake" forState:UIControlStateNormal];
    [_retakeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [[_retakeButton titleLabel] setFont:[UIFont systemFontOfSize:18]];
    [_retakeButton addTarget:self action:@selector(retake) forControlEvents:UIControlEventTouchUpInside];
    [previewoverlay addSubview:_retakeButton];
    
    _useButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_useButton setBackgroundImage:[UIImage imageNamed:@"www/img/cameraoverlay/back_button.png"] forState:UIControlStateNormal];
    [_useButton setBackgroundImage:[UIImage imageNamed:@"www/img/cameraoverlay/back_button_pressed.png"] forState:UIControlStateHighlighted];
    [_useButton setTitle:@"Use" forState:UIControlStateNormal];
    [_useButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [[_useButton titleLabel] setFont:[UIFont systemFontOfSize:18]];
    [_useButton addTarget:self action:@selector(use) forControlEvents:UIControlEventTouchUpInside];
    [previewoverlay addSubview:_useButton];
    
    return previewoverlay;
}

- (void)viewWillLayoutSubviews {
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        [self layoutForTablet];
    } else {
        [self layoutForPhone];
    }
}

- (void)layoutForPhone {
    CGRect bounds = [[UIScreen mainScreen] bounds];
    
    _captureButton.frame = CGRectMake((bounds.size.width / 2) - (kCaptureButtonWidthPhone / 2),
                                      bounds.size.height - kCaptureButtonHeightPhone - kCaptureButtonVerticalInsetPhone,
                                      kCaptureButtonWidthPhone,
                                      kCaptureButtonHeightPhone);
    
    _backButton.frame = CGRectMake((CGRectGetMinX(_captureButton.frame) - kBackButtonWidthPhone) / 2,
                                   CGRectGetMinY(_captureButton.frame) + ((kCaptureButtonHeightPhone - kBackButtonHeightPhone) / 2),
                                   kBackButtonWidthPhone,
                                   kBackButtonHeightPhone);
    
    _useButton.frame = CGRectMake((CGRectGetMaxX(_captureButton.frame) + kUseButtonWidthPhone),
                                   CGRectGetMinY(_captureButton.frame) + ((kCaptureButtonHeightPhone - kBackButtonHeightPhone) / 2),
                                   kUseButtonWidthPhone,
                                   kBackButtonHeightPhone);
    
    _buttonPanel.frame = CGRectMake(0,
                                    CGRectGetMinY(_captureButton.frame) - kCaptureButtonVerticalInsetPhone,
                                    bounds.size.width,
                                    kCaptureButtonHeightPhone + (kCaptureButtonVerticalInsetPhone * 2));
    
    _retakeButton.frame = _backButton.frame;
    _previewButtonPanel.frame = _buttonPanel.frame;

    
    CGFloat screenAspectRatio = bounds.size.height / bounds.size.width;
    if (screenAspectRatio <= 1.5f) {
        [self layoutForPhoneWithShortScreen];
    } else {
        [self layoutForPhoneWithTallScreen];
    }
}

- (void)layoutForPhoneWithShortScreen {
    CGRect bounds = [[UIScreen mainScreen] bounds];
    CGFloat verticalInset = 5;
    CGFloat height = CGRectGetMinY(_buttonPanel.frame) - (verticalInset * 2);
    CGFloat width = height / kAspectRatio;
    CGFloat horizontalInset = (bounds.size.width - width) / 2;
    
    horizontalInset = kHorizontalInsetPhone4s;
    _topLeftGuide.frame = CGRectMake(horizontalInset,
                                     verticalInset,
                                     kBorderImageWidthPhone,
                                     kBorderImageHeightPhone);
    
    _topRightGuide.frame = CGRectMake(bounds.size.width - kBorderImageWidthPhone - horizontalInset,
                                      verticalInset,
                                      kBorderImageWidthPhone,
                                      kBorderImageHeightPhone);
/*
    _bottomLeftGuide.frame = CGRectMake(CGRectGetMinX(_topLeftGuide.frame),
                                        CGRectGetMinY(_topLeftGuide.frame) + height - kBorderImageHeightPhone,
                                        kBorderImageWidthPhone,
                                        kBorderImageHeightPhone);
    
    _bottomRightGuide.frame = CGRectMake(CGRectGetMinX(_topRightGuide.frame),
                                         CGRectGetMinY(_topRightGuide.frame) + height - kBorderImageHeightPhone,
                                         kBorderImageWidthPhone,
                                         kBorderImageHeightPhone);
*/
}

- (void)layoutForPhoneWithTallScreen {
    CGRect bounds = [[UIScreen mainScreen] bounds];
    _topLeftGuide.frame = CGRectMake(kHorizontalInsetPhone, kVerticalInsetPhone, kBorderImageWidthPhone, kBorderImageHeightPhone);
    
    _topRightGuide.frame = CGRectMake(bounds.size.width - kBorderImageWidthPhone - kHorizontalInsetPhone,
                                      kVerticalInsetPhone,
                                      kBorderImageWidthPhone,
                                      kBorderImageHeightPhone);
    
//    CGFloat height = (CGRectGetMaxX(_topRightGuide.frame) - CGRectGetMinX(_topLeftGuide.frame)) * kAspectRatio;
/*
    _bottomLeftGuide.frame = CGRectMake(CGRectGetMinX(_topLeftGuide.frame),
                                        CGRectGetMinY(_topLeftGuide.frame) + height - kBorderImageHeightPhone,
                                        kBorderImageWidthPhone,
                                        kBorderImageHeightPhone);
    
    _bottomRightGuide.frame = CGRectMake(CGRectGetMinX(_topRightGuide.frame),
                                         CGRectGetMinY(_topRightGuide.frame) + height - kBorderImageHeightPhone,
                                         kBorderImageWidthPhone,
                                         kBorderImageHeightPhone);
*/
}

- (void)layoutForTablet {
    CGRect bounds = [[UIScreen mainScreen] bounds];
    
    _captureButton.frame = CGRectMake((bounds.size.width / 2) - (kCaptureButtonWidthTablet / 2),
                                      bounds.size.height - kCaptureButtonHeightTablet - kCaptureButtonVerticalInsetTablet,
                                      kCaptureButtonWidthTablet,
                                      kCaptureButtonHeightTablet);
    
    _backButton.frame = CGRectMake((CGRectGetMinX(_captureButton.frame) - kBackButtonWidthTablet) / 2,
                                   CGRectGetMinY(_captureButton.frame) + ((kCaptureButtonHeightTablet - kBackButtonHeightTablet) / 2),
                                   kBackButtonWidthTablet,
                                   kBackButtonHeightTablet);
    
    _useButton.frame = CGRectMake((CGRectGetMaxX(_captureButton.frame) + kUseButtonWidthTablet),
                                   CGRectGetMinY(_captureButton.frame) + ((kCaptureButtonHeightTablet - kBackButtonHeightTablet) / 2),
                                   kUseButtonWidthTablet,
                                   kBackButtonHeightTablet);
    
    _buttonPanel.frame = CGRectMake(0,
                                    CGRectGetMinY(_captureButton.frame) - kCaptureButtonVerticalInsetTablet,
                                    bounds.size.width,
                                    kCaptureButtonHeightTablet + (kCaptureButtonVerticalInsetTablet * 2));

    _retakeButton.frame = _backButton.frame;
    _previewButtonPanel.frame = _buttonPanel.frame;
    
    
    _topLeftGuide.frame = CGRectMake(kHorizontalInsetTablet, kVerticalInsetTablet, kBorderImageWidthTablet, kBorderImageHeightTablet);
    
    _topRightGuide.frame = CGRectMake(bounds.size.width - kBorderImageWidthTablet - kHorizontalInsetTablet,
                                      kVerticalInsetTablet,
                                      kBorderImageWidthTablet,
                                      kBorderImageHeightTablet);
    
//    CGFloat height = (CGRectGetMaxX(_topRightGuide.frame) - CGRectGetMinX(_topLeftGuide.frame)) * kAspectRatio;
    
/*
    _bottomLeftGuide.frame = CGRectMake(CGRectGetMinX(_topLeftGuide.frame),
                                        CGRectGetMinY(_topLeftGuide.frame) + height - kBorderImageHeightTablet,
                                        kBorderImageWidthTablet,
                                        kBorderImageHeightTablet);
    
    _bottomRightGuide.frame = CGRectMake(CGRectGetMinX(_topRightGuide.frame),
                                         CGRectGetMinY(_topRightGuide.frame) + height - kBorderImageHeightTablet,
                                         kBorderImageWidthTablet,
                                         kBorderImageHeightTablet);
 */
}

- (void)viewDidLoad {

}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [[UIApplication sharedApplication] setStatusBarHidden:YES];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
}

- (BOOL)prefersStatusBarHidden {
    return YES;
}

- (NSUInteger)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (UIInterfaceOrientation)preferredInterfaceOrientationForPresentation {
    return UIInterfaceOrientationPortrait;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return orientation == UIDeviceOrientationPortrait;
}

- (void)dismissCameraPreview {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (AVCaptureConnection*)videoConnectionToOutput:(AVCaptureOutput*)output {
    for (AVCaptureConnection *connection in output.connections) {
        for (AVCaptureInputPort *port in [connection inputPorts]) {
            if ([[port mediaType] isEqual:AVMediaTypeVideo]) {
                return connection;
            }
        }
    }
    return nil;
}


- (void) preview
{

//    [[[self captureManager] session] stopRunning];
    _imagePreview.contentMode = UIViewContentModeScaleAspectFill;
    [_imagePreview setImage:[UIImage imageWithData:_imageData]];

    [self.view addSubview:_previewView];
}

- (void) retake
{
    [_previewView removeFromSuperview];
    
//    [[[self captureManager] session] startRunning];
}

- (void) use
{
    _activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    _activityIndicator.center = self.view.center;
    [self.view addSubview:_activityIndicator];
    [_activityIndicator startAnimating];
    
    _useButton.userInteractionEnabled = NO;
    _retakeButton.userInteractionEnabled = NO;
    _callback([UIImage imageWithData:_imageData]);
}

@end
