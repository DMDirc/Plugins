/*
 * Copyright (c) 2006-2014 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dmdirc.addons.dcc.io;

import com.dmdirc.addons.dcc.DCCTransferHandler;
import com.dmdirc.util.collections.ListenerList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles a DCC transfer.
 *
 * @author Shane 'Dataforce' McCormack
 */
public class DCCTransfer extends DCC {

    /** List of active sends. */
    private static final List<DCCTransfer> TRANSFERS = new ArrayList<>();

    /** File Transfer Types. */
    public enum TransferType {

        SEND, RECEIVE;

    }
    /** The File transfer type for this file. */
    private TransferType transferType = TransferType.RECEIVE;
    /** The handlers for this DCCSend. */
    private final ListenerList handlers = new ListenerList();
    /** Used to send data out the socket. */
    private DataOutputStream out;
    /** Used to read data from the socket. */
    private DataInputStream in;
    /** File we are using. */
    private File transferFile;
    /** Used to write data to the file. */
    private DataOutputStream fileOut;
    /** Used to read data from the file. */
    private DataInputStream fileIn;
    /** Where are we starting from? */
    private int startpos;
    /** How big is this file? */
    private long size = -1;
    /** How much of this file have we read so far? */
    private long readSize;
    /** What is the name of the file? */
    private String filename = "";
    /** What is the token for this send? */
    private String token = "";
    /** Block Size. */
    private final int blockSize;
    /** Is this a turbo dcc? */
    private boolean turbo = false;
    private boolean active = false;

    /** Creates a new instance of DCCTransfer with a default block size. */
    public DCCTransfer() {
        this(1024);
    }

    /**
     * Creates a new instance of DCCTransfer.
     *
     * @param blockSize Block size to use
     */
    public DCCTransfer(final int blockSize) {
        super();
        this.blockSize = blockSize;
        synchronized (TRANSFERS) {
            TRANSFERS.add(this);
        }
    }

    /**
     * Reset this send to be used again (eg a resend).
     */
    public void reset() {
        close();
        setFileName(filename);
        setFileStart(startpos);
        synchronized (TRANSFERS) {
            TRANSFERS.add(this);
        }
    }

    /**
     * Get a copy of the list of active sends.
     *
     * @return A copy of the list of active sends.
     */
    public static List<DCCTransfer> getTransfers() {
        synchronized (TRANSFERS) {
            return new ArrayList<>(TRANSFERS);
        }
    }

    /**
     * Called to remove this object from the sends list.
     */
    public void removeFromTransfers() {
        synchronized (TRANSFERS) {
            TRANSFERS.remove(this);
        }
    }

    /**
     * Set the filename of this file
     *
     * @param filename Filename
     */
    public void setFileName(final String filename) {
        this.filename = filename;
        if (transferType == TransferType.SEND) {
            transferFile = new File(filename);
            try {
                fileIn = new DataInputStream(new FileInputStream(transferFile.getAbsolutePath()));
            } catch (FileNotFoundException | SecurityException e) {
                fileIn = null;
            }
        }
    }

    /**
     * Get the filename of this file
     *
     * @return Filename
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Get the filename of this file, without the path
     *
     * @return Filename without path
     */
    public String getShortFileName() {
        return new File(filename).getName();
    }

    /**
     * Set dcc Type.
     *
     * @param type Type of DCC transfer this is.
     */
    public void setType(final TransferType type) {
        this.transferType = type;
    }

    /**
     * Get dcc Type.
     *
     * @return Type of DCC transfer this is.
     */
    public TransferType getType() {
        return transferType;
    }

    /**
     * Set turbo mode on/off. Turbo mode doesn't wait for ack packets. Only relevent when sending.
     *
     * @param turbo True for turbo dcc, else false
     */
    public void setTurbo(final boolean turbo) {
        this.turbo = turbo;
    }

    /**
     * Is turbo mode on/off. Turbo mode doesn't wait for ack packets. Only relevent when sending.
     *
     * @return True for turbo dcc, else false
     */
    public boolean isTurbo() {
        return turbo;
    }

    /**
     * Set the Token for this send
     *
     * @param token Token for this send
     */
    public void setToken(final String token) {
        this.token = token;
    }

    /**
     * Get the Token for this send
     *
     * @return Token for this send
     */
    public String getToken() {
        return token;
    }

    /**
     * Make a Token for this send. This token will be unique compared to all the other known sends
     *
     * @return The Token for this send.
     */
    public String makeToken() {
        String myToken = "";
        do {
            myToken = Integer.toString(Math.abs((myToken + filename).hashCode()));
        } while (findByToken(myToken) != null);
        setToken(myToken);
        return myToken;
    }

    /**
     * Find a send based on a given token.
     *
     * @param token Token to look for. (case sensitive)
     *
     * @return The first DCCTransfer that matches the given token. null if none match, or token is
     *         "" or null.
     */
    public static DCCTransfer findByToken(final String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        for (DCCTransfer transfer : getTransfers()) {
            if (transfer.getToken().equals(token)) {
                return transfer;
            }
        }
        return null;
    }

    /**
     * Set the size of the file
     *
     * @param size File size
     */
    public void setFileSize(final long size) {
        this.size = size;
    }

    /**
     * Get the expected size of the file
     *
     * @return The expected File size (-1 if unknown)
     */
    public long getFileSize() {
        return size;
    }

    /**
     * Set the starting position of the file
     *
     * @param startpos Starting position
     *
     * @return -1 if fileIn is null or if dcc receive, else the result of fileIn.skipBytes()
     */
    public int setFileStart(final int startpos) {
        this.startpos = startpos;
        this.readSize = startpos;

        if (transferType == TransferType.SEND && fileIn != null) {
            try {
                this.startpos = fileIn.skipBytes(startpos);
                this.readSize = this.startpos;
                return this.startpos;
            } catch (IOException ioe) {
            }
        }

        return -1;
    }

    /**
     * Get the starting position of the file
     *
     * @return starting position of file.
     */
    public int getFileStart() {
        return this.startpos;
    }

    /**
     * Change the handler for this DCC Send
     *
     * @param handler A class implementing DCCTransferHandler
     */
    public void addHandler(final DCCTransferHandler handler) {
        handlers.add(DCCTransferHandler.class, handler);
    }

    @Override
    protected void socketOpened() {
        try {
            active = true;
            transferFile = new File(filename);
            if (transferType == TransferType.RECEIVE) {
                fileOut = new DataOutputStream(new FileOutputStream(
                        transferFile.getAbsolutePath(), (startpos > 0)));
            }
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());

            for (DCCTransferHandler handler : handlers.get(DCCTransferHandler.class)) {
                handler.socketOpened(this);
            }
        } catch (IOException ioe) {
            socketClosed();
        }
    }

    @Override
    protected void socketClosed() {
        // Try to close both, even if one fails.
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        out = null;
        in = null;

        for (DCCTransferHandler handler : handlers.get(DCCTransferHandler.class)) {
            handler.socketClosed(this);
        }
        // Try to delete empty files.
        if (transferType == TransferType.RECEIVE && transferFile != null
                && transferFile.length() == 0) {
            transferFile.delete();
        }
        synchronized (TRANSFERS) {
            TRANSFERS.remove(this);
        }
        active = false;
    }

    @Override
    protected boolean handleSocket() {
        if (out == null || in == null) {
            return false;
        }
        if (transferType == TransferType.RECEIVE) {
            return handleReceive();
        } else {
            return handleSend();
        }
    }

    /**
     * Handle the socket as a RECEIVE.
     *
     * @return false when socket is closed (or should be closed), true will cause the method to be
     *         called again.
     */
    protected boolean handleReceive() {
        try {
            final byte[] data = new byte[blockSize];
            final int bytesRead = in.read(data);
            readSize = readSize + bytesRead;

            if (bytesRead > 0) {
                for (DCCTransferHandler handler : handlers.get(DCCTransferHandler.class)) {
                    handler.dataTransfered(this, bytesRead);
                }
                fileOut.write(data, 0, bytesRead);

                if (!turbo) {
                    // Send ack
                    out.writeInt((int) readSize);
                    out.flush();
                }

                if (readSize == size) {
                    fileOut.close();

                    if (turbo) {
                        in.close();
                    }

                    return false;
                } else {
                    return true;
                }
            } else if (bytesRead < 0) {
                fileOut.close();
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Handle the socket as a SEND.
     *
     * @return false when socket is closed (or should be closed), true will cause the method to be
     *         called again.
     */
    protected boolean handleSend() {
        try {
            final byte[] data = new byte[blockSize];
            final int bytesRead = fileIn.read(data);
            readSize += bytesRead;

            if (bytesRead > 0) {
                for (DCCTransferHandler handler : handlers.get(DCCTransferHandler.class)) {
                    handler.dataTransfered(this, bytesRead);
                }
                out.write(data, 0, bytesRead);
                out.flush();

                // Wait for acknowlegement packet.
                if (!turbo) {
                    int bytesRecieved;
                    do {
                        bytesRecieved = in.readInt();
                    } while ((readSize - bytesRecieved) > 0);
                }

                if (readSize == size) {
                    fileIn.close();

                    // Process all the ack packets that may have been sent.
                    // In true turbo dcc mode, none will have been sent and the socket
                    // will just close, in fast-dcc mode all the acks will be here,
                    // So keep reading acks untill the socket closes (IOException) or we
                    // have recieved all the acks.
                    if (turbo) {
                        int ack = 0;
                        do {
                            try {
                                ack = in.readInt();
                            } catch (IOException e) {
                                break;
                            }
                        } while (ack > 0 && (readSize - ack) > 0);
                    }

                    return false;
                }

                return true;
            } else if (bytesRead < 0) {
                fileIn.close();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    /**
     * Is this DCC transfer active.
     *
     * @return true iif active
     */
    public boolean isActive() {
        return active;
    }

}
