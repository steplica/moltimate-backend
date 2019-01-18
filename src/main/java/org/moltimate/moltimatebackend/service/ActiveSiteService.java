package org.moltimate.moltimatebackend.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import org.moltimate.moltimatebackend.model.ActiveSite;
import org.moltimate.moltimatebackend.model.Residue;
import org.moltimate.moltimatebackend.util.HttpUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service used to query for or create active site Residues. Also provides the functionality to update our database's
 * active site table.
 */
@Service
@Validated
@Slf4j
public class ActiveSiteService {

    private static final String CSA_CSV_URL = "https://www.ebi.ac.uk/thornton-srv/m-csa/media/flat_files/curated_data.csv";

    /**
     * Retrieve all active sites from the Catalytic Site Atlas.
     *
     * @return A list of ActiveSites
     */
    public List<ActiveSite> getActiveSites() {
        try (Reader catalyticSiteAtlasCsvData = new StringReader(HttpUtils.readStringFromURL(CSA_CSV_URL));
             CSVReader csvReader = new CSVReaderBuilder(catalyticSiteAtlasCsvData).withSkipLines(1)
                     .build()
        ) {
            List<ActiveSite> activeSites = new ArrayList<>();

            ActiveSite nextSite;
            while ((nextSite = readNextActiveSite(csvReader)) != null) {
                activeSites.add(nextSite);
            }

            return activeSites;
        } catch (IOException e) {
            log.error("Error reading catalytic site atlas curated data file");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Reads the next protein's active site residues from the Catalytic Site Atlas curated data file.
     */
    private ActiveSite readNextActiveSite(CSVReader csvReader) throws IOException {
        List<Residue> activeSiteResidues = new ArrayList<>();

        String[] residueEntry;
        while ((residueEntry = csvReader.readNext()) != null) {
            String pdbId = residueEntry[2];

            boolean isResidue = "residue".equals(residueEntry[4]);
            if (isResidue) {
                Residue residue = Residue.builder()
                        .residueName(residueEntry[5])
                        .residueChainName(residueEntry[6])
                        .residueId(residueEntry[7])
                        .build();
                if (!activeSiteResidues.contains(residue)) {
                    activeSiteResidues.add(residue);
                }
            }

            // If the next row is the end of file OR start of a different protein, stop and return current Residue list
            String[] nextEntry = csvReader.peek();
            if (nextEntry == null || !nextEntry[2].equals(pdbId)) {
                return ActiveSite.builder()
                        .pdbId(pdbId)
                        .residues(activeSiteResidues)
                        .build();
            }
        }

        return null;
    }
}
