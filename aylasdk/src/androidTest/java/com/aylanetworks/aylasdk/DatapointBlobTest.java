package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DatapointBlobTest {
    AylaDevice _device;
    private String _localInputfilePath;
    private String _localOutputfilePath;
    private static final String LOG_TAG = "DatapointBlobTest";
    private String [] _propertyNames = new String[1];

    @Before
    public void setUp() {
        TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME)
                .getDeviceManager();
        _device = deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(_device);

        //Make sure there is a file for blob testing
        String path = InstrumentationRegistry.getContext().getDataDir().toString() + "/DatapointBlobTest";
        //Check if directory exists
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            boolean result=fileDir.mkdir();
            if(!result) {
                fail("failed to make dir");
            }

        }

        File file = new File(path, "blobInput.txt");
        if (!file.exists()) {
            writeToFile(file);
        }
        _localInputfilePath = file.getPath();
        assertNotNull(_localInputfilePath);
        _propertyNames[0]= TestConstants.TEST_DEVICE_PROPERTY_STREAM_UP;
    }

    @Test
    public void testUploadDatapointBlob() {
        AylaProperty property=null;
        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();

        _device.fetchPropertiesCloud(_propertyNames,futureProperty,futureProperty);
        try {
            AylaProperty[] properties= futureProperty.get();
            if(properties !=null && properties.length>0) {
                property = properties[0];
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }
        assertNotNull(property);

        RequestFuture<AylaDatapointBlob> future = RequestFuture.newFuture();
        property.createDatapoint(null, null, future, future);

        AylaDatapointBlob datapointBlob = null;
        try {
            datapointBlob = future.get();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }

        assertNotNull(datapointBlob);

        RequestFuture<AylaAPIRequest.EmptyResponse> futureResp = RequestFuture.newFuture();
        FileProgress fileProgressUpload = new FileProgress();
        datapointBlob.uploadBlob(fileProgressUpload,_localInputfilePath, futureResp, futureResp);
        try {
            futureResp.get();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }

        _localOutputfilePath = new File(InstrumentationRegistry.getContext().getDataDir(),
                "blobOutTest.txt").getAbsolutePath();
        RequestFuture<AylaAPIRequest.EmptyResponse> futureDownResp = RequestFuture.newFuture();
        FileProgress fileProgressDownload = new FileProgress();
        datapointBlob.downloadToFile(_localOutputfilePath, fileProgressDownload,futureDownResp,
                futureDownResp);
        int API_TIMEOUT_MS = 20000;
        try {
            futureDownResp.get(API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }

    }

    private void writeToFile(File file) {
        String string = "Hamlet Summary provides a quick review of the play's plot including every " +
                "important action in the play. Hamlet Summary is divided by the five acts of the play and is an ideal introduction before reading the original text.\n" +
                "\n" +
                "Act I.\n" +
                "\n" +
                "Shakespeare's longest play and the play responsible for the immortal lines \"To be or not to be: that is the question:\" and the advise \"to thine own self be true,\" begins in Denmark with the news that King Hamlet of Denmark has recently died.\n" +
                "\n" +
                "Denmark is now in a state of high alert and preparing for possible war with Young Fortinbras of Norway. A ghost resembling the late King Hamlet is spotted on a platform before Elsinore Castle in Denmark. King Claudius, who now rules Denmark, has taken King Hamlet's wife, Queen Gertrude as his new wife and Queen of Denmark.\n" +
                "\n" +
                "King Claudius fearing Young Fortinbras of Norway may invade, has sent ambassadors to Norway to urge the King of Norway to restrain Young Fortinbras. Young Hamlet distrusts King Claudius. The King and Queen do not understand why Hamlet still mourns his father's death over two months ago. In his first soliloquy, Hamlet explains that he does not like his mother marrying the next King of Denmark so quickly within a month of his father's death...\n" +
                "\n" +
                "Laertes, the son of Lord Chamberlain Polonius, gives his sister Ophelia some brotherly advice. He warns Ophelia not to fall in love with Young Hamlet; she will only be hurt. Polonius tells his daughter Ophelia not to return Hamlet's affections for her since he fears Hamlet is only using her...\n" +
                "\n" +
                "Hamlet meets the Ghost of his father, King Hamlet and follows it to learn more...\n" +
                "\n" +
                "Hamlet learns from King Hamlet's Ghost that he was poisoned by King Claudius, the current ruler of Denmark. The Ghost tells Hamlet to avenge his death but not to punish Queen Gertrude for remarrying; it is not Hamlet's place and her conscience and heaven will judge her... Hamlet swears Horatio and Marcellus to silence over Hamlet meeting the Ghost.\n" +
                "\n" +
                "Act II.\n" +
                "\n" +
                "Polonius tells Reynaldo to spy on his son Laertes in Paris. Polonius learns from his daughter Ophelia that a badly dressed Hamlet met her, studied her face and promptly left. Polonius believes that Hamlet's odd behavior is because Ophelia has rejected him. Polonius decides to tell King Claudius the reason for Hamlet's recently odd behavior.\n" +
                "\n" +
                "King Claudius instructs courtiers Rosencrantz and Guildenstern to find out what is causing Hamlet's strange \"transformation,\" or change of character. Queen Gertrude reveals that only King Hamlet's death and her recent remarriage could be upsetting Hamlet.\n" +
                "\n" +
                "We learn more of Young Fortinbras' movements and Polonius has his own theory about Hamlet's transformation; it is caused by Hamlet's love for his daughter Ophelia. Hamlet makes his famous speech about the greatness of man. Hamlet plans to use a play to test if King Claudius really did kill his father as King Hamlet's Ghost told him...\n" +
                "\n" +
                "Act III.\n" +
                "\n" +
                "The King's spies, Rosencrantz and Guildenstern report to King Claudius on Hamlet's behavior. Hamlet is eager for King Claudius and Queen Gertrude to watch a play tonight which Hamlet has added lines to.\n" +
                "\n" +
                "King Claudius and Polonius listen in on Hamlet's and Ophelia's private conversation. Hamlet suspects Ophelia is spying on him and is increasingly hostile to her before leaving.\n" +
                "\n" +
                "King Claudius decides to send Hamlet to England, fearing danger in Hamlet since he no longer believes Hamlet is merely lovesick. The King agrees to Polonius' plan to eavesdrop on Hamlet's conversation with his mother after the play to hopefully learn more from Hamlet. The play Hamlet had added lines to is performed. The mime preceding the play which mimics the Ghost's description of King Hamlet's death goes unnoticed.\n" +
                "\n" +
                "The main play called \"The Murder of Gonzago\" is performed, causing King Claudius to react in a way which convinces Hamlet that his uncle did indeed poison his father King Hamlet as the Ghost previously had told him... Hamlet pretends not to know that the play has offended King Claudius. Hamlet agrees to speak with his mother in private...\n" +
                "\n" +
                "King Claudius admits his growing fear of Hamlet and decides to send him overseas to England with Rosencrantz and Guildenstern in order to protect himself. Alone, King Claudius reveals in soliloquy his own knowledge of the crime he has committed (poisoning King Hamlet) and realizes that he cannot escape divine justice...\n" +
                "\n" +
                "Queen Gertrude attempts to scold her son but Hamlet instead scolds his mother for " +
                "her actions. Queen Gertrude cries out in fear, and Polonius echoes it and is stabbed through the arras (subdivision of a room created by a hanging tapestry) where he was listening in. Hamlet continues scolding his mother but the Ghost reappears, telling Hamlet to be gentle with the Queen. For her part, Queen Gertrude agrees to stop living with King Claudius, beginning her redemption....\n" +
                "Act IV.\n" +
                "\n" +
                "King Claudius speaks with his wife, Queen Gertrude. He learns of Polonius' murder which shocks him; it could easily have been him. Queen Gertrude lies for her son, saying that Hamlet is as mad as a tempestuous sea. King Claudius, now scared of Hamlet, decides to have Hamlet sent away to England immediately... He also sends courtiers and spies Rosencrantz and Guildenstern to speak with Hamlet to find out where Hamlet has hidden Polonius' body so they can take it to the chapel.\n" +
                "\n" +
                "Hamlet refuses to tell Rosencrantz and Guildenstern where Polonius' dead body is hidden. He calls Rosencrantz and Guildenstern lapdogs revealing his true awareness that they are not his friends. Hamlet agrees to see King Claudius.\n" +
                "\n" +
                "Hamlet continues to refuse to tell Rosencrantz and Guildenstern where Polonius' body is. Hamlet is brought before the King. The two exchange words, clearly circling each other, each aware that the other is a threat. Hamlet tells King Claudius where Polonius body is. King Claudius ominously tells Hamlet to leave for England supposedly for Hamlet's own safety. With Hamlet gone, King Claudius reveals his plans for Hamlet to be killed in England, freeing King Claudius from further worry from this threat...\n" +
                "\n" +
                "Young Fortinbras marches his army across Denmark to fight the Polish. Hamlet laments that he does not have in him the strength of Young Fortinbras, who will lead an army into pointless fighting, if only to maintain honor. Hamlet asks himself how he cannot fight for honor when his father has been killed and his mother made a whore in his eyes by becoming King Claudius' wife.\n" +
                "\n" +
                "The death of Polonius leaves its mark on Ophelia who becomes mad from the grief of losing her father. Laertes storms King Claudius' castle, demanding to see his father and wanting justice when he learns that his father, Polonius has been killed. King Claudius remains calm, telling Laertes that he too mourned his father's loss...\n" +
                "\n" +
                "Horatio is greeted by sailors who have news from Hamlet. Horatio follows the sailors to learn more... King Claudius explains to Laertes that Hamlet killed his father, Polonius. Deciding they have a common enemy, they plot Hamlet's death at a fencing match to be arranged between Laertes and Hamlet. Laertes learns of his sister Ophelia's death by drowning...\n" +
                "\n" +
                "Act V.\n" +
                "\n" +
                "Hamlet and Horatio speak with a cheerful Clown or gravedigger. Hamlet famously realizes that man's accomplishments are transitory (fleeting) and holding the skull of Yorick, a childhood jester he remembered, creates a famous scene about man's insignificance and inability to control his fate following death.\n" +
                "\n" +
                "At Ophelia's burial, the Priest reveals a widely held belief that Ophelia committed suicide, angering Laertes. Hamlet fights Laertes over Ophelia's grave, angered by Laertes exaggerated emphasis of his sorrow and because he believes he loved Ophelia much more than her brother.\n" +
                "\n" +
                "Hamlet explains to Horatio how he avoided the death planned for him in England and had courtiers' Rosencrantz and Guildenstern put to death instead. Hamlet reveals his desire to kill King Claudius.\n" +
                "\n" +
                "Summoned by Osric to fence against Laertes, Hamlet arrives at a hall in the castle and fights Laertes. Queen Gertrude drinks a poisoned cup meant for Hamlet, dying but not before telling all that she has been poisoned.\n" +
                "\n" +
                "Hamlet wins the first two rounds against Laertes but is stabbed and poisoned fatally in the third round. Exchanging swords whilst fighting, Hamlet wounds and poisons Laertes who explains that his sword is poison tipped.\n" +
                "\n" +
                "Now dying, Hamlet stabs King Claudius with this same sword, killing him.\n" +
                "\n" +
                "Hamlet, dying, tells Horatio to tell his story and not to commit suicide. Hamlet recommends Young Fortinbras as the next King of Denmark. Young Fortinbras arrives, cleaning up the massacre. Horatio promises to tell all the story we have just witnessed, ending the play.\n";
        try {
            boolean result = file.createNewFile();
            if(!result) {
                fail("Create new file failed");
            }
            PrintWriter writer = new PrintWriter(file);
            writer.write(string);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        File file = new File(_localOutputfilePath);
        if (file.exists()) {
            //file.delete();
        }
        file = new File(_localInputfilePath);
        if (file.exists()) {
            //file.delete();
        }
    }

    private class FileProgress implements MultipartProgressListener {
        public void updateProgress(long value1,long total) {
            AylaLog.d(LOG_TAG, "File Progress is " +value1 +" out of " +total);
        }
        public boolean isCanceled() {
            return false;
        }
    }
}
