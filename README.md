# MyScratchView

刮奖效果的总体思路是，利用framelayout的重叠特性，将刮层和图层分别定义。
先定义图层，然后在定义刮层，这样刮层才可以在上面。

然后对刮层自定义，初始化一种颜色，然后利用PorterDuff.Mode.CLEAR模式，当
在刮层上移动时，用该模式进行涂写，可以达到清除的效果。

1. 擦除模式
    mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    
2. 计算擦除百分比
    被擦除的像素 其实是透明的，透明的像素值是0。利用这点可以计算出有多少透明像素的个数，
    然后与像素总数向除，得到一个百分比，即得到擦除的百分比
    
    //像素总素，即宽X高
    int totalPixels = width * height;
    //获取搜有像素的值，注意是用Bitmap对象获取的，因为绘制都是在Bitmap上的
    int[] mPixels = new int[w * h];
    mMaskBitmap.getPixels(mPixels,0,width,0,0,width,height);
    
    //计算擦除的像素数
    for (int pos = 0; pos < totalPixels; pos ++){
        if (mPixels[pos] == 0){ //透明的像素的值是0
            erasedPixels++;
        }
    }
    
    
