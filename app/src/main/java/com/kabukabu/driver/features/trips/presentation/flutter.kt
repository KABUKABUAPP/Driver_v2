//package com.kabukabu.driver.features.trips.presentation
//
//import 'dart:io';
//
//import 'package:dotted_line/dotted_line.dart';
//import 'package:flutter/material.dart';
//import 'package:flutter_rating_bar/flutter_rating_bar.dart';
//import 'package:flutter_screenutil/flutter_screenutil.dart';
//import 'package:kabukabu_driver/common/components/action_container.dart';
//
//import 'package:kabukabu_driver/common/components/app_image.dart';
//import 'package:flutter_share/flutter_share.dart';
//import 'package:path_provider/path_provider.dart';
//import 'package:screenshot/screenshot.dart';
//import 'package:share_plus/share_plus.dart';
//import 'package:kabukabu_driver/common/components/general/app_header.dart';
//import 'package:kabukabu_driver/common/components/spacing.dart';
//import 'package:kabukabu_driver/common/constant/app_assets.dart';
//import 'package:kabukabu_driver/common/constant/size_constant.dart';
//import 'package:kabukabu_driver/common/constant/text_constant.dart';
//import 'package:kabukabu_driver/common/constant/util.dart';
//import 'package:kabukabu_driver/common/themes/app_colors.dart';
//import 'package:kabukabu_driver/helpers/styles.dart';
//import 'package:kabukabu_driver/helpers/utilities.dart';
//
//class TripReceiptView extends StatefulWidget {
//    final dynamic data;
//    TripReceiptView({this.data, super.key});
//
//    @override
//    State<TripReceiptView> createState() => _TripReceiptViewState();
//}
//
//class _TripReceiptViewState extends State<TripReceiptView> {
//    ScreenshotController screenshotController = ScreenshotController();
//    @override
//    Widget build(BuildContext context) {
//        return Scaffold(
//            backgroundColor: AppColors.white,
//        body: SafeArea(
//        child: Container(
//        color: AppColors.white,
//        height: scaledHeight(context).toDouble().h,
//        margin: EdgeInsets.only(
//        left: Sizes.dimen_15.w,
//        right: Sizes.dimen_15.w,
//        ),
//        child: Column(
//        crossAxisAlignment: CrossAxisAlignment.stretch,
//        mainAxisAlignment: MainAxisAlignment.start,
//        children: [
//        VerticalSpacing(30.h),
//        AppHeader(
//            title: TextLiterals.receipt,
//        onClick: () => Navigator.of(context).pop(),
//        ),
//        VerticalSpacing(40.h),
//        Expanded(
//            child: SingleChildScrollView(
//                    child: Screenshot(
//                    controller: screenshotController,
//            child: Container(
//                    padding: EdgeInsets.all(
//                Sizes.dimen_10.r,
//        ),
//        decoration: BoxDecoration(
//        color: AppColors.white,
//        borderRadius: BorderRadius.circular(15.r),
//        boxShadow: [
//        BoxShadow(
//            color: AppColors.black.withOpacity(0.15),
//        blurRadius: 5,
//        spreadRadius: 2,
//        )
//        ],
//        ),
//        child: Column(
//        mainAxisAlignment: MainAxisAlignment.start,
//        crossAxisAlignment: CrossAxisAlignment.start,
//        children: [
//        VerticalSpacing(10.h),
//        Styles.bold(
//            TextLiterals.appName,
//            height: 2.0.h,
//        fontSize: scaledFontSize(20.h, context),
//        ),
//        VerticalSpacing(10.h),
//        Styles.medium(
//            TextLiterals.yourTripDetails,
//            fontSize: scaledFontSize(13.sp, context),
//        ),
//        VerticalSpacing(20.h),
//        Row(
//            children: [
//            Column(
//                mainAxisAlignment: MainAxisAlignment.start,
//            crossAxisAlignment: CrossAxisAlignment.center,
//        children: [
//        VerticalSpacing(10.h),
//        AppImage(
//            path: AppAssets.ellipse,
//        height: 20.0.h,
//        width: 20.0.h,
//        ),
//        DottedLine(
//            direction: Axis.vertical,
//        lineLength: 70.w,
//        dashColor: AppColors.grey95,
//        ),
//        AppImage(
//            path: AppAssets.locationAlt,
//        height: 12.0.h,
//        width: 12.0.w,
//        ),
//        ],
//        ),
//        HorizontalSpacing(10.r),
//        SizedBox(
//            width: 230.w,
//        child: Column(
//        mainAxisAlignment: MainAxisAlignment.start,
//        crossAxisAlignment:
//        CrossAxisAlignment.stretch,
//        children: [
//        Styles.medium(
//            widget.data.startAddress?.street ?? '',
//        height: 1.5.h,
//        fontSize:
//        scaledFontSize(15.sp, context),
//        fontWeight: FWt.semiBold),
//        VerticalSpacing(15.h),
//        DottedLine(
//            dashColor: AppColors.grey95,
//        ),
//        VerticalSpacing(15.h),
//        Column(
//            crossAxisAlignment:
//            CrossAxisAlignment.start,
//        children: [
//        Styles.medium(
//            widget.data.endAddress?.street ??
//        '',
//        height: 1.5.h,
//        fontSize:
//        scaledFontSize(15.sp, context),
//        fontWeight: FWt.semiBold),
//        Styles.medium(
//            widget.data.endAddress?.state ?? '',
//        fontSize:
//        scaledFontSize(13.sp, context),
//        color: AppColors.grey95,
//        ),
//        ],
//        ),
//        ],
//        ),
//        ),
//        ],
//        ),
//        const Divider(
//                color: AppColors.grey95,
//        ),
//        SizedBox(
//            height: 35.h,
//        width: scaledWidth(context).w,
//        child: Stack(
//        children: [
//        SizedBox(
//            height: 30.h,
//        child: Row(
//        mainAxisAlignment:
//        MainAxisAlignment.spaceBetween,
//        children: [
//        Styles.medium(
//            Utilities.tripDate(
//                widget.data.createdAt!),
//            fontSize:
//            scaledFontSize(10.sp, context),
//        color: AppColors.grey95,
//        ),
//        Row(
//            children: [
//            AppImage(
//                path: AppAssets.wallet,
//            color: AppColors.grey95
//        .withOpacity(0.13),
//        height: 15.0.h,
//        width: 15.0.w,
//        ),
//        HorizontalSpacing(5.w),
//        Styles.medium(
//            widget.data.paymentType ?? '',
//        fontSize:
//        scaledFontSize(13.sp, context),
//        color: AppColors.grey95,
//        ),
//        ],
//        )
//        ],
//        ),
//        )
//        ],
//        ),
//        ),
//        const Divider(
//                color: AppColors.grey95,
//        ),
//        VerticalSpacing(10.h),
//        Row(
//            mainAxisAlignment: MainAxisAlignment.spaceBetween,
//        children: [
//        Styles.regular(TextLiterals.youEarn,
//            fontSize: scaledFontSize(15.sp, context),
//        color: AppColors.grey95,
//        fontWeight: FWt.semiBold),
//        Styles.regular(
//            Utilities.formatAmount(
//                amount: widget
//                .data.priceDetails.driverEarned
//                    .toDouble())
//            .toString(),
//        fontSize: scaledFontSize(17.sp, context),
//        color: AppColors.transparentBlack,
//        fontWeight: FWt.semiBold),
//        ],
//        ),
//        VerticalSpacing(20.h),
//        Row(
//            mainAxisAlignment: MainAxisAlignment.spaceBetween,
//        children: [
//        Styles.regular(TextLiterals.actualPrice,
//            fontSize: scaledFontSize(15.sp, context),
//        color: AppColors.grey95,
//        fontWeight: FWt.semiBold),
//        Styles.regular(
//            Utilities.formatAmount(
//                amount: widget
//                .data.priceDetails.totalCharge
//                    .toDouble())
//            .toString(),
//        fontSize: scaledFontSize(14.sp, context),
//        color: AppColors.transparentBlack,
//        ),
//        ],
//        ),
//        VerticalSpacing(20.h),
//        Row(
//            mainAxisAlignment: MainAxisAlignment.spaceBetween,
//        children: [
//        Styles.regular(TextLiterals.bookingFee,
//            fontSize: scaledFontSize(15.sp, context),
//        color: AppColors.grey95,
//        fontWeight: FWt.semiBold),
//        Styles.regular(
//            Utilities.formatAmount(
//                amount: widget
//                .data.priceDetails.bookingFee
//                    .toDouble())
//            .toString(),
//        fontSize: scaledFontSize(14.sp, context),
//        color: AppColors.transparentBlack,
//        ),
//        ],
//        ),
//        VerticalSpacing(20.h),
//        Row(
//            mainAxisAlignment: MainAxisAlignment.spaceBetween,
//        children: [
//        Styles.regular(TextLiterals.riderVat,
//            fontSize: scaledFontSize(15.sp, context),
//        color: AppColors.grey95,
//        fontWeight: FWt.semiBold),
//        Styles.regular(
//            Utilities.formatAmount(
//                amount: widget
//                .data.priceDetails.stateLevy
//                    .toDouble())
//            .toString(),
//        fontSize: scaledFontSize(14.sp, context),
//        color: AppColors.transparentBlack,
//        ),
//        ],
//        ),
//        const Divider(
//                color: AppColors.grey95,
//        ),
//        SizedBox(
//            height: 95.h,
//        width: scaledWidth(context).w,
//        child: Stack(
//        children: [
//        Container(
//            height: 95.h,
//        padding: const EdgeInsets.only(
//        left: Sizes.dimen_15),
//        child: Row(
//        children: [
//        CircleAvatar(
//            radius: 30.r,
//        backgroundImage: widget.data.user
//        .profileImage ==
//            null ||
//            widget.data.user.profileImage ==
//            ""
//        ? const AssetImage(
//            AppAssets.avatar,
//        )
//        : NetworkImage(
//        widget.data.user.profileImage!,
//    ) as ImageProvider,
//        ),
//        HorizontalSpacing(15.w),
//        Expanded(
//            child: Column(
//                    mainAxisAlignment:
//            MainAxisAlignment.center,
//        crossAxisAlignment:
//        CrossAxisAlignment.start,
//        children: [
//        VerticalSpacing(7.h),
//        Styles.semiBold(
//            Utilities.linedFullName(widget
//                .data.user.fullName
//                .trim()),
//        ),
//        FittedBox(
//            child: Row(
//                    children: [
//            RatingBarIndicator(
//                rating: widget.data.user
//                    .averageRating.value
//                    .toDouble(),
//            itemCount: 5,
//        itemSize: 30.0,
//        itemBuilder: (context, _) =>
//        const Icon(
//                Icons.star_rounded,
//        color: AppColors
//        .green),
//        ),
//        ],
//        ),
//        ),
//        VerticalSpacing(7.h),
//        Styles.medium(
//            "${widget.data.driver.totalTrips} ${TextLiterals.totalTrips}",
//            fontSize: scaledFontSize(
//                    15.sp, context),
//        ),
//        ],
//        ),
//        ),
//        ],
//        ),
//        )
//        ],
//        ),
//        ),
//        ],
//        ),
//        ),
//        ),
//        ),
//        ),
//        VerticalSpacing(Sizes.dimen_10),
//        ActionContainer(
//            mainAxisAlignment: MainAxisAlignment.center,
//        height: 50.h,
//        width: scaledWidth(context).w,
//        color: AppColors.primaryYellow,
//        isExpandedText: false,
//        onTap: () async {
//        await screenshotController.capture().then((image) async {
//            if (image != null) {
//                final directory =
//                await getApplicationDocumentsDirectory();
//                final imagePath =
//                await File('${directory.path}/image.png').create();
//                await imagePath.writeAsBytes(image);
//
//                Platform.isIOS
//                ? await FlutterShare.shareFile(
//                        title:
//                "Kabukabu trip receipt_${DateTime.now().toIso8601String()}",
//                filePath: imagePath.path,
//                )
//                : await Share.shareXFiles([XFile(imagePath.path)],
//                text:
//                "Kabukabu trip receipt_${DateTime.now().toIso8601String()}");
//            }
//        });
//        // showModalBottomSheet(
//        //     context: context,
//        //     elevation: 2.0,
//        //     isDismissible: true,
//        //     isScrollControlled: true,
//        //     shape: RoundedRectangleBorder(
//        //       borderRadius: BorderRadius.only(
//        //           topRight: Radius.circular(Sizes.dimen_10.r),
//        //           topLeft: Radius.circular(Sizes.dimen_10.r)),
//        //     ),
//        //     builder: (context) {
//        //       return const ShareTripBottomSheet();
//        //     });
//    },
//        icon: AppAssets.shareIcon,
//        text: TextLiterals.shareReceipt,
//        ),
//        VerticalSpacing(10.h),
//        ],
//        ),
//        ),
//        ),
//        );
//    }
//}
