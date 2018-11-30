/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

//
//  AppDelegate.m
//  MyApp
//
//  Created by ___FULLUSERNAME___ on ___DATE___.
//  Copyright ___ORGANIZATIONNAME___ ___YEAR___. All rights reserved.
//

#import "AppDelegate.h"
#import "MainViewController.h"
#import "YunBaService.h"
#import "CDVYBPushPlugin.h"
@implementation AppDelegate

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    
    self.viewController = [[MainViewController alloc] init];
    return [super application:application didFinishLaunchingWithOptions:launchOptions];
}
// for device token
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    
//    NSLog(@"get Device Token: %@", [NSString stringWithFormat:@"Device Token: %@", deviceToken]);
    // uncomment to store device token to YunBa
//    [YunBaService storeDeviceToken:deviceToken resultBlock:^(BOOL succ, NSError *error) {
//        if (succ) {
//            NSLog(@"store device token to YunBa succ");
//        } else {
//            NSLog(@"store device token to YunBa failed due to : %@, recovery suggestion: %@", error, [error localizedRecoverySuggestion]);
//        }
//    }];
}

/**
 收到通知的回调
 
 @param application  UIApplication 实例
 @param userInfo 推送时指定的参数
 */
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo {
    if (self.modifyHeadPicBlock && userInfo[@"aps"][@"alert"]) {
        self.modifyHeadPicBlock(userInfo[@"aps"][@"alert"]);
    }else{
        
        [self performSelector:@selector(delayDo:) withObject:userInfo[@"aps"][@"alert"] afterDelay:2.0f];
    }
  
}

// iOS 10 新增 API
// iOS 10 会走新 API, iOS 10 以前会走到老 API
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
-(void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler{
    if (self.modifyHeadPicBlock && userInfo[@"aps"][@"alert"]) {
        self.modifyHeadPicBlock(userInfo[@"aps"][@"alert"]);
    }else{

        [self performSelector:@selector(delayDo:) withObject:userInfo[@"aps"][@"alert"] afterDelay:2.0f];
 
    }

    completionHandler(UIBackgroundFetchResultNewData);
}
#endif


-(void)delayDo:(id)sender{
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ClickOn_Navigationbar" object:sender];
}


- (void)applicationWillEnterForeground:(UIApplication *)application{
    
    [self clearBadgeAndNotifications];
    
}
- (void)clearBadgeAndNotifications {
    if ([UIApplication sharedApplication].applicationIconBadgeNumber) {
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    } else {
        // for occasions like push a notification with alert but with no badge num, then the code above shall not work for cleaning ios notification center
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:1];
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    }
}

@end

