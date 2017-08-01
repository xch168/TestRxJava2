package com.github.xch168.testrxjava2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TimeUtils;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** RxJava Operation*/
        //createObservable();
        //mapOp();
        //zipOp();
        //concatOp();
        //flatMapOp();
        //concatMapOp();
        //distinctOp();
        //filterOp();
        //bufferOp();
        //timerOp();
        //intervalOp();
        //doOnNextOp();
        //skipOp();
        //takeOp();
        //justOp();
        //singleTest();
        //debounceOp();
        //deferOp();
        //lastOp();
        //mergeOp();
        //reduceOp();
        //scanOp();
        //windowOp();

        /** RxJava practices*/
        //requestNetwork();
    }

    private void requestNetwork() {
        
        Observable.create(new ObservableOnSubscribe<Response>() {
            @Override
            public void subscribe(ObservableEmitter<Response> e) throws Exception {
                // 调用OKHttp网络请求
                Request.Builder builder = new Request.Builder()
                        .url("http://api.avatardata.cn/MobilePlace/LookUp?key=ec47b85086be4dc8b5d941f5abd37a4e&mobileNumber=13021671512")
                        .get();
                Request request = builder.build();
                Call call = new OkHttpClient().newCall(request);
                Response response = call.execute();
                e.onNext(response);
            }
        }).map(new Function<Response, MobileAddress>() {
            @Override
            public MobileAddress apply(Response response) throws Exception {
                // 通过Gson将Response转换成bean类
                Log.i(TAG, "map thread:" + Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        Log.i(TAG, "map:转换前:" + response.body());
                        return new Gson().fromJson(body.string(), MobileAddress.class);
                    }
                }
                return null;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(MobileAddress mobileAddress) throws Exception {
                        // 解析bean中的数据，并进行数据库存储等操作
                        Log.i(TAG, "doOnNext thread:" + Thread.currentThread().getName());
                        Log.i(TAG, "doOnNext:保存成功:" + mobileAddress.toString());
                        throw new IOException("ssss");
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MobileAddress>() {
                    @Override
                    public void accept(MobileAddress mobileAddress) throws Exception {
                        Log.i(TAG, "subscribe thread:" + Thread.currentThread().getName());
                        Log.i(TAG, "成功：" + mobileAddress.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "subscribe thread:" + Thread.currentThread().getName());
                        Log.e(TAG, "失败:" + throwable.getMessage());
                    }
                });
    }

    /**
     * 按实际划分窗口，将数据发送给不同的Observable
     */
    private void windowOp() {
        Observable.interval(1, TimeUnit.SECONDS)
                .take(15)
                .window(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Observable<Long>>() {
                    @Override
                    public void accept(Observable<Long> longObservable) throws Exception {
                        Log.i(TAG, "Sub Divide begin...");
                        longObservable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        Log.i(TAG, "Next:" + aLong);
                                    }
                                });
                    }
                });
    }

    /**
     * 和reduce操作一致，区别在于reduce关注的是结果，而scan会把每个步骤都输出
     */
    private void scanOp() {
        Observable.just(1, 2 , 3)
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "accept: scan:" + integer);
                    }
                });
    }

    /**
     * 每次用一个方法处理一个值
     */
    private void reduceOp() {
        Observable.just(1, 2, 3, 4)
                .reduce(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer, Integer integer2) throws Exception {
                        Log.i(TAG, "i:" + integer + " i2:" + integer2);
                        return integer + integer2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "accept:reduce:" + integer);
                    }
                });
    }

    /**
     * 用于把躲过Observable结合起来，接受可变参数，
     * 和concat的区别在于，不用等到发射器A发送完所有的事件再进行发射器B的发送
     */
    private void mergeOp() {
        Observable.merge(Observable.just(1, 2), Observable.just(3, 4, 5), Observable.just(6, 7))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "accept: merge:" + integer);
                    }
                });
    }

    /**
     * 获取客观察到的最后一个值
     */
    private void lastOp() {

        List<Integer> list = new ArrayList<>();
        list.add(1);
        Observable.fromIterable(list)
                .last(10)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "last:" + integer);
                    }
                });
    }

    /**
     * 每次订阅都会创建一个新的Observable
     */
    private void deferOp() {
        Observable<Integer> observable = Observable.defer(new Callable<ObservableSource<? extends Integer>>() {
            @Override
            public ObservableSource<? extends Integer> call() throws Exception {
                return Observable.just(1, 2, 3);
            }
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                Log.i(TAG, "defer:" + integer);
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "defer: onError:" + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "defer: onComplete");
            }
        });
    }

    /**
     * 去除发送频率过快的项
     */
    private void debounceOp() {
        Observable.create(new ObservableOnSubscribe<Integer>() {

            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                Thread.sleep(100);
                e.onNext(2);
                Thread.sleep(200);
                e.onNext(3);
                Thread.sleep(300);
                e.onNext(4);
                Thread.sleep(400);
                e.onNext(5);
                Thread.sleep(500);
                e.onComplete();
            }
        }).debounce(200, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "debounce:" + integer);
                    }
                });
    }

    /**
     * Single只会接收一个参数，SingleObserver只会调用onError()或者onSuccess()
     */
    private void singleTest() {
        Single.just(new Random().nextInt())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i(TAG, "disposable:" + d.isDisposed());
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.i(TAG, "single : onSuccess:" + integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "single: onError:" + e.getMessage());
                    }
                });
    }


    /**
     * 发射事件
     */
    private void justOp() {
        Observable.just("1", "2", "3")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.i(TAG, "accept:" + s);
                    }
                });
    }


    /**
     * 获取指定数量的事件
     */
    private void takeOp() {
        Flowable.fromArray(1, 2, 3, 4, 5)
                .take(3)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "accept: take:" + integer);
                    }
                });
    }

    /**
     * 跳过指定数量的事件
     */
    private void skipOp() {
        Observable.just(1, 2, 3, 4, 5)
                .skip(2)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "skip:" + integer);
                    }
                });
    }

    /**
     * 让订阅者在接收到数据之前干点有意义的事，如获取到数据后先保存一下
     */
    private void doOnNextOp() {
        Observable.just(1, 2, 3, 4)
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "doOnNext:" + integer);
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "subscribe:" + integer);
                    }
                });
    }

    /**
     * 周期任务
     */
    private void intervalOp() {
        Observable.interval(3, 2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.i(TAG, "interval:" + aLong);
                    }
                });
    }


    /**
     * 定时任务
     */
    private void timerOp() {
        Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Log.i(TAG, "timer:" + aLong);
                    }
                });
    }

    /**
     * buffer操作，count：表示每个buffer的大小，skip：表示每次跳过skip个事件
     */
    private void bufferOp() {
        Observable.just(1, 2, 3, 4, 5, 6, 7)
                .buffer(5, 3)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) throws Exception {
                        Log.i(TAG, "buffer size:" + integers.size());
                        Log.i(TAG, "buffer value:");
                        for (Integer i : integers) {
                            Log.i(TAG, i + "");
                        }
                    }
                });
    }

    /**
     * 过滤操作
     */
    private void filterOp() {
        Observable.just(1, 20, 65, -1, 8, 99)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer >= 20;
                    }
                }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Log.i(TAG, "filter:" + integer);
            }
        });
    }

    /**
     * 去重操作
     */
    private void distinctOp() {
        Observable.just(1, 1, 2, 2, 3, 4, 5)
                .distinct()
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "distinct:" + integer);
                    }
                });
    }

    /**
     * 操作和flatMap一样，但是是有序的
     */
    private void concatMapOp() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer + " / " + i);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.i(TAG, "concatMap: accept:" + s);
                    }
                });
    }

    /**
     * 将每个发射的事件转换成一个Observable，然后把这些Observable装进一个单一的发射器Observable，不能保证事件的顺序
     */
    private void flatMapOp() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                Log.i(TAG, "thread:" + Thread.currentThread().getName());
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.i(TAG, "flatMap: accept:" + s);
                    }
                });
    }

    /**
     * 连接操作，用来将两个发射器连接成一个发射器
     */
    private void concatOp() {
        Observable.concat(Observable.just(1, 2, 3), Observable.just(4, 5, 6))
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG, "concat:" + integer);
                    }
                });
    }

    /**
     * zip操作，用于合并事件，两两配对，配对数于发射事件最少的Observable相同
     */
    private void zipOp() {
        Observable.zip(getStringObservable(), getIntegerObservable(), new BiFunction<String, Integer, String>() {
            @Override
            public String apply(String s, Integer integer) throws Exception {
                return s + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) throws Exception {
                Log.i(TAG, "zip: accept:" + o);
            }
        });
    }

    private Observable<String> getStringObservable() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                if (!e.isDisposed()) {
                    Log.i(TAG, "String emit: A");
                    e.onNext("A");
                    Log.i(TAG, "String emit: B");
                    e.onNext("B");
                    Log.i(TAG, "String emit: C");
                    e.onNext("C");
                }
            }
        });
    }


    private Observable<Integer> getIntegerObservable() {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                if (!e.isDisposed()) {
                    Log.i(TAG, "Integer emit 1");
                    e.onNext(1);
                    Log.i(TAG, "Integer emit 2");
                    e.onNext(2);
                    Log.i(TAG, "Integer emit 3");
                    e.onNext(3);
                    Log.i(TAG, "Integer emit 4");
                    e.onNext(4);
                    Log.i(TAG, "Integer emit 5");
                    e.onNext(5);
                }
            }
        });
    }


    /**
     * map操作，对每个发射的事件执行一个函数变换
     */
    private void mapOp() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "This is result " + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i(TAG, "accept:" + s);
            }
        });
    }

    private void createObservable() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.i(TAG, "Observable emit 1");
                e.onNext(1);
                Log.i(TAG, "Observable emit 2");
                e.onNext(2);
                Log.i(TAG, "Observable emit 3");
                e.onNext(3);
                e.onComplete(); // onComplete执行后，后面的方法仍继续执行，只是Observer不能接收事件
                Log.i(TAG, "Observable emit 4");
                e.onNext(4);

            }
        }).subscribe(new Observer<Integer>() {

            private int i;
            private Disposable mDisposable; // 用于停止接收事件

            @Override // 该方法的执行先于ObservableOnSubscribe.subscribe
            public void onSubscribe(Disposable d) {
                Log.i(TAG, "onSubscribe:" + d.isDisposed());
                mDisposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                Log.e(TAG, "onNext: value:" + integer);
                i++;
                if (i == 2) {
                    mDisposable.dispose(); // 停止接收事件
                    Log.e(TAG, "onNext: isDisposable:" + mDisposable.isDisposed());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: value:" + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete");
            }
        });
    }
}
