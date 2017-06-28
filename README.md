# SimplifySwipeBackLayout
简化SwipeBackLayout,减少嵌套层级,优化性能,将子view滑动改为自身的滑动  灵感来自于:https://github.com/ikew0ng/SwipeBackLayout

####使用方式
```kotlin
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
        //保证windowIsTranslucent为true且windowBackground为透明时使用
        setContentView(SimplifySwipeBackLayout(this).apply {
            addView(View(context))
            setEnableGesture(true)
            setSwipeListener {
                finish()
            }
        })
    }

}
```