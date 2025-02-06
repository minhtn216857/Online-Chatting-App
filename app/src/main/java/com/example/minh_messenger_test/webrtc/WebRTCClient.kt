package com.example.minh_messenger_test.webrtc

import android.content.Context
import android.util.Log
import com.example.minh_messenger_test.utils.DataModel
import com.example.minh_messenger_test.utils.DataModelType
import com.google.gson.Gson
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCClient @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {

    // Class variables
    var listener: Listener? = null
    private lateinit var username: String

    // WebRTC variables
    private val eglBaseContext = EglBase.create().eglBaseContext  // Tạo EGL context để render video WebRTC
    private val peerConnectionFactory by lazy { createPeerConnectionFactory() }  // Factory tạo các PeerConnection
    private var peerConnection: PeerConnection? = null  // Đối tượng PeerConnection để quản lý kết nối

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:ss-turn2.xirsys.com").createIceServer(),
        PeerConnection.IceServer.builder("turn:ss-turn2.xirsys.com:80?transport=udp")
            .setUsername("YfuG9ImT3ARNhhbVfIzOGOz_qIFtomA-x-8dLJ6ZSs8zElI8dhvLkvHRfaG1jGAIAAAAAGeiEqBtaW5oMzU1MjU=")
            .setPassword("ea7b3e6a-e2f9-11ef-aa87-0242ac140004")
            .createIceServer(),
        PeerConnection.IceServer.builder("turn:ss-turn2.xirsys.com:3478?transport=udp")
            .setUsername("YfuG9ImT3ARNhhbVfIzOGOz_qIFtomA-x-8dLJ6ZSs8zElI8dhvLkvHRfaG1jGAIAAAAAGeiEqBtaW5oMzU1MjU=")
            .setPassword("ea7b3e6a-e2f9-11ef-aa87-0242ac140004")
            .createIceServer(),
        PeerConnection.IceServer.builder("turns:ss-turn2.xirsys.com:443?transport=tcp")
            .setUsername("YfuG9ImT3ARNhhbVfIzOGOz_qIFtomA-x-8dLJ6ZSs8zElI8dhvLkvHRfaG1jGAIAAAAAGeiEqBtaW5oMzU1MjU=")
            .setPassword("ea7b3e6a-e2f9-11ef-aa87-0242ac140004")
            .createIceServer(),
        PeerConnection.IceServer.builder("turns:ss-turn2.xirsys.com:5349?transport=tcp")
            .setUsername("YfuG9ImT3ARNhhbVfIzOGOz_qIFtomA-x-8dLJ6ZSs8zElI8dhvLkvHRfaG1jGAIAAAAAGeiEqBtaW5oMzU1MjU=")
            .setPassword("ea7b3e6a-e2f9-11ef-aa87-0242ac140004")
            .createIceServer()
    )

    // Hỗ trợ xử lý texture từ video.
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private val localAudioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) } // Tạo nguồn âm thanh
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) } // Tạo nguồn video (false = không bật mặc định)

    private val videoCapturer = getVideoCapturer(context)
    private val mediaContraint = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))  // Yêu cầu nhận video.
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }

    // Call variables
    private lateinit var localSurfaceView: SurfaceViewRenderer  // View hiển thị video cục bộ (local)
    private lateinit var remoteSurfaceView: SurfaceViewRenderer // View hiển thị video từ xa (remote)
    private var localStream: MediaStream? = null                // Stream chứa các track media (audio/video)
    private var localTrackId = ""                               // ID cho track cục bộ
    private var localStreamId = ""                              // ID cho stream cục bộ
    private var localAudioTrack: AudioTrack? = null             // Track âm thanh cục bộ
    private var localVideoTrack: VideoTrack? = null             // Track video cục bộ

    // Initializing WebRTC dependencies
    init {
        initPeerConnectionFactory()  // Khởi tạo PeerConnectionFactory khi class được tạo
    }
    /**
     * Khởi tạo PeerConnectionFactory với các cấu hình mặc định
     */
    private fun initPeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true) // Bật trình theo dõi nội bộ để debug
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/") // Kích hoạt hỗ trợ mã hóa H.264 High Profile
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }
    /**
     * Tạo PeerConnectionFactory để quản lý các kết nối WebRTC
     */
    private fun createPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBaseContext))  // Xử lý giải mã video
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBaseContext, true, true))  // Xử lý mã hóa video
            .setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = false // Bật/tắt theo dõi mạng
                disableEncryption = false     // Bật mã hóa dữ liệu
            })
            .createPeerConnectionFactory()
    }

    /**
     * Khởi tạo WebRTC Client với tên người dùng và Observer để lắng nghe sự kiện từ PeerConnection
     */
    fun initializeWebrtcClient(username: String, observer: PeerConnection.Observer) {
        this.username = username
        localTrackId = "${username}_track"
        localStreamId = "${username}_stream"
        peerConnection = createPeerConnection(observer)

        // Kiểm tra danh sách ràng buộc (constraints)
        Log.d("WebRTC1", "🔧 Media Constraints: ${mediaContraint.mandatory}")
    }



    /**
     * Tạo kết nối PeerConnection với cấu hình ICE Server và Observer
     */
    private fun createPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        Log.d("WebRTC1", "🛠️ Đang tạo PeerConnection với observer: $observer")
        return peerConnectionFactory.createPeerConnection(iceServer, observer)
    }


    fun call(target: String){
        Log.d("WebRTC1", "📞 Bắt đầu gửi Offer đến $target")

        peerConnection?.createOffer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                Log.d("WebRTC1", "✅ Offer được tạo: ${desc?.description}")
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        Log.d("WebRTC1", "📡 Đã đặt Local Description")
                        listener?.onTransferEventToSocket(
                            DataModel(type = DataModelType.Offer,
                                sender = username,
                                target = target,
                                data = desc?.description)
                        )
                    }
                }, desc)
            }

        }, mediaContraint)
    }

    fun answer(target: String){
        peerConnection?.createAnswer(object : MySdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                super.onCreateSuccess(desc)
                peerConnection?.setLocalDescription(object : MySdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        listener?.onTransferEventToSocket(
                            DataModel(type = DataModelType.Answer,
                                sender = username,
                                target = target,
                                data = desc?.description)
                        )
                    }
                }, desc)
            }

        }, mediaContraint)
    }

    fun onRemoteSessionReceived(sessionDescription: SessionDescription){
        peerConnection?.setRemoteDescription(MySdpObserver(), sessionDescription)
    }

    fun addIceCandidateToPeer(iceCandidate: IceCandidate){
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun sendIceCandidate(target: String, iceCandidate: IceCandidate){
        addIceCandidateToPeer(iceCandidate)
        listener?.onTransferEventToSocket(
            DataModel(type = DataModelType.IceCandidates,
                sender = username,
                target = target,
                data = gson.toJson(iceCandidate))
        )
    }

    fun closeConnection(){
        try {
            videoCapturer.dispose()
            localStream?.dispose()
            peerConnection?.dispose()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun switchCamera(){
        videoCapturer.switchCamera(null)
    }

    fun toggleAudio(shouldBeMuted: Boolean){
        if(shouldBeMuted){
            localStream?.removeTrack(localAudioTrack)
        }else{
            localStream?.addTrack(localAudioTrack)
        }
    }

    fun toggleVideo(shouldBeMuted: Boolean){
        try{
            if(shouldBeMuted){
                stopCapturingCamera()
            }else{
                startCapturingCamera(localSurfaceView)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }




    // ----- Streaming Section -----
    /**
     * Khởi tạo SurfaceView để hiển thị video (cục bộ hoặc từ xa)
     */
    private fun initSurfaceView(view: SurfaceViewRenderer) {
        view.run {
            setMirror(false)                 // Không lật gương video
            setEnableHardwareScaler(true)    // Bật tăng tốc phần cứng để cải thiện hiệu suất
            init(eglBaseContext, null)       // Khởi tạo SurfaceView với EGL context
        }
    }
    fun initRemoteSurfaceView(view: SurfaceViewRenderer){
        this.remoteSurfaceView = view
        initSurfaceView(view)
    }
    /**
     * Khởi tạo SurfaceView cục bộ và bắt đầu streaming (video hoặc chỉ audio)
     */
    fun initLocalSurfaceView(localView: SurfaceViewRenderer, isVideoCall: Boolean) {
        this.localSurfaceView = localView
        initSurfaceView(localView)
        startLocalStreaming(localView, isVideoCall)
    }
    /**
     * Bắt đầu truyền phát media cục bộ (audio và video nếu là cuộc gọi video)
     */
    private fun startLocalStreaming(localView: SurfaceViewRenderer, isVideoCall: Boolean) {
        localStream = peerConnectionFactory.createLocalMediaStream(localStreamId)

        if (isVideoCall) {
            startCapturingCamera(localView)
        }

        localAudioTrack = peerConnectionFactory.createAudioTrack(localTrackId + "_audio", localAudioSource)
        localStream?.addTrack(localAudioTrack)

        Log.d("WebRTC1", "Đang thêm Local Stream vào PeerConnection...")
        peerConnection?.addStream(localStream)

    }

    /**
     * Bắt đầu quay video từ camera
     */
    private fun startCapturingCamera(localView: SurfaceViewRenderer) {
        Log.d("WebRTC1", "🎥 Bắt đầu quay video từ Camera...")

        surfaceTextureHelper = SurfaceTextureHelper.create(
            Thread.currentThread().name, eglBaseContext
        )

        videoCapturer.initialize(
            surfaceTextureHelper, context, localVideoSource.capturerObserver
        )

        videoCapturer.startCapture(
            720, 480, 20
        )

        localVideoTrack = peerConnectionFactory.createVideoTrack(localTrackId+"_video", localVideoSource)
        localVideoTrack?.addSink(localView)
        localStream?.addTrack(localVideoTrack)
    }

    private fun getVideoCapturer(context: Context): CameraVideoCapturer =
        Camera2Enumerator(context).run{
            deviceNames.find{
                isFrontFacing(it)
            }?.let{
                createCapturer(it, null)
            }?:throw IllegalStateException()
        }
    /**
     * Dừng quay video từ camera
     */
    private fun stopCapturingCamera() {
        videoCapturer.dispose()
        localVideoTrack?.removeSink(localSurfaceView)
        localSurfaceView.clearImage()
        localStream?.removeTrack(localAudioTrack)
        localVideoTrack?.dispose()
    }



    /**
     * Interface để gửi dữ liệu qua socket hoặc xử lý sự kiện WebRTC
     */
    interface Listener {
        fun onTransferEventToSocket(data: DataModel)
    }
}
