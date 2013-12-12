package vace117.creeper.webrtc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Callback methods for native code that puts together the SDP Offer
 * TODO: Rename to SdpOfferObserver?
 * 
 * This implementation uses a <code>CountDownLatch</code> in order to makes sure that if 
 * we call getSdp() before the native code has returned a proper <code>SessionDescription</code>,
 * the method will block and wait for a second.
 * 
 * @author Val Blant
 */
public class SdpObserverImpl implements SdpObserver {
	private SessionDescription sdp = null;
	private String error = null;
	private boolean success = false;
	private CountDownLatch latch = new CountDownLatch(1);

	@Override
	public void onCreateSuccess(SessionDescription sdp) {
		this.sdp = sdp;
		onSetSuccess();
	}

	@Override
	public void onSetSuccess() {
		success = true;
		latch.countDown();
	}

	@Override
	public void onCreateFailure(String error) {
		onSetFailure(error);
	}

	@Override
	public void onSetFailure(String error) {
		this.error = error;
		latch.countDown();
	}

	public SessionDescription getSdp() {
		try {
			latch.await(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return sdp;
	}

	public void setSdp(SessionDescription sdp) {
		this.sdp = sdp;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
