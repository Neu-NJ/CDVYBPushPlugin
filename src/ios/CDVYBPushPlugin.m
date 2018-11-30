/********* CDVYBPushPlugin.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import "YunBaService.h"
#import <UserNotifications/UserNotifications.h>
#import "AppDelegate.h"

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
#import <UserNotifications/UserNotifications.h>
#endif

@interface CDVYBPushPlugin : CDVPlugin<UNUserNotificationCenterDelegate>
@property (nonatomic ,copy)NSString *topicMessage;
@property (nonatomic ,copy)NSString *callbackId;
- (void)addSubscribe:(CDVInvokedUrlCommand*)command;
- (void)topSkip:(CDVInvokedUrlCommand*)command;
- (void)unSubscribe:(CDVInvokedUrlCommand*)command;
- (void)addAlias:(CDVInvokedUrlCommand*)command;

@end

@implementation CDVYBPushPlugin

/**
 * 插件初始化
 */
- (void) pluginInitialize {
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(clickOnNavigationbar:) name: @"ClickOn_Navigationbar" object:nil];
    
    [YunBaService setupWithAppkey:[self getAccessKey]];
    [self registerRemoteNotification];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onMessageReceived:) name:kYBDidReceiveMessageNotification object:nil];
    
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    center.delegate = self;
    
    AppDelegate *appDel = (AppDelegate *)[UIApplication sharedApplication].delegate;
    appDel.modifyHeadPicBlock = ^(NSString *picPath){
        
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:picPath];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        
    };
    
}


/**
 * 添加订阅
 */
- (void) addSubscribe:(CDVInvokedUrlCommand*)command {
    
    [YunBaService subscribe:[command.arguments objectAtIndex:0] resultBlock:^(BOOL succ, NSError *error){
        if (succ) {
            
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"addSubscribe succeed"];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        } else {
            
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }];
    
}
/**
 * 取消订阅
 */
- (void) unSubscribe:(CDVInvokedUrlCommand*)command {
    
    [YunBaService unsubscribe:[command.arguments objectAtIndex:0] resultBlock:^(BOOL succ, NSError *error){
        if (succ) {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"unSubscribe succeed"];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
            
        } else {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }];
    
}
/**
 * 添加别名
 */
-(void)addAlias:(CDVInvokedUrlCommand*)command{
    
    [YunBaService setAlias:[command.arguments objectAtIndex:0] resultBlock:^(BOOL succ, NSError *error) {
        if (succ) {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"addAlias succeed"];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        } else {
            CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
            [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
        }
    }];
}
//点击推送栏返回结果到ionic
- (void) topSkip:(CDVInvokedUrlCommand*)command {
    
    self.callbackId = command.callbackId;
    
    
}
-(void)clickOnNavigationbar:(id)sender{
    
    if (sender) {
        
        CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[sender object]];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    }else{
        CDVPluginResult*  result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"error"];
        [result setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
        
    }
}


- (void)onMessageReceived:(NSNotification *)notification {
    YBMessage *message = [notification object];
    NSString *payloadString = [[NSString alloc] initWithData:[message data] encoding:NSUTF8StringEncoding];
    NSLog(@"[Message] %@ => %@", [message topic], payloadString);
    self.topicMessage = [message topic];
    
}

//前台推送栏提示
-(void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler{
    completionHandler(UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
}

#pragma mark - apns & device token

- (void)registerRemoteNotification {
    // register for remote notification(APNs)     注册 APNs，申请获取 device token
    
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 10.0)
    {
        UNUserNotificationCenter* center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert + UNAuthorizationOptionSound + UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error) {
            granted ? NSLog(@"author success!") : NSLog(@"author failed!");
        }];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else if ([[[UIDevice currentDevice] systemVersion] floatValue] < 10.0 &&
             [[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0)
    {
        [[UIApplication sharedApplication] registerUserNotificationSettings:[UIUserNotificationSettings
                                                                             settingsForTypes:(UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge) categories:nil]];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else
    {
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
    }
}


- (NSString*) getAccessKey {
    return [[[NSBundle mainBundle] objectForInfoDictionaryKey:@"YBPushMeta"] valueForKey:@"AccessKey"];
}

@end

