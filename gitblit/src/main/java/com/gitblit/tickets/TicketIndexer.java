//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.gitblit.tickets;

import com.gitblit.manager.IRuntimeManager;
import com.gitblit.models.RepositoryModel;
import com.gitblit.models.TicketModel;
import com.gitblit.models.TicketModel.Attachment;
import com.gitblit.models.TicketModel.Patchset;
import com.gitblit.models.TicketModel.Priority;
import com.gitblit.models.TicketModel.Severity;
import com.gitblit.models.TicketModel.Status;
import com.gitblit.models.TicketModel.Type;
import com.gitblit.utils.FileUtils;
import com.gitblit.utils.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TicketIndexer {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Version luceneVersion;
    private final File luceneDir;
    private IndexWriter writer;
    private IndexSearcher searcher;

    public TicketIndexer(IRuntimeManager runtimeManager) {
        this.luceneVersion = Version.LUCENE_5_5_2;
        this.luceneDir = runtimeManager.getFileOrFolder("tickets.indexFolder", "${baseFolder}/tickets/lucene");
    }

    public void close() {
        this.closeSearcher();
        this.closeWriter();
    }

    public void deleteAll() {
        this.close();
        FileUtils.delete(this.luceneDir);
    }

    public boolean deleteAll(RepositoryModel repository) {
        try {
            IndexWriter writer = this.getWriter();
            StandardAnalyzer analyzer = new StandardAnalyzer();
            QueryParser qp = new QueryParser(Lucene.rid.name(), analyzer);
            BooleanQuery query = new BooleanQuery();
            query.add(qp.parse(repository.getRID()), Occur.MUST);
            int numDocsBefore = writer.numDocs();
            writer.deleteDocuments(new Query[]{query});
            writer.commit();
            this.closeSearcher();
            int numDocsAfter = writer.numDocs();
            if (numDocsBefore == numDocsAfter) {
                this.log.debug(MessageFormat.format("no records found to delete in {0}", repository));
                return false;
            } else {
                this.log.debug(MessageFormat.format("deleted {0} records in {1}", numDocsBefore - numDocsAfter, repository));
                return true;
            }
        } catch (Exception var8) {
            this.log.error("error", var8);
            return false;
        }
    }

    public void index(List<TicketModel> tickets) {
        try {
            IndexWriter writer = this.getWriter();
            Iterator var3 = tickets.iterator();

            while (var3.hasNext()) {
                TicketModel ticket = (TicketModel) var3.next();
                Document doc = this.ticketToDoc(ticket);
                writer.addDocument(doc);
            }

            writer.commit();
            this.closeSearcher();
        } catch (Exception var6) {
            this.log.error("error", var6);
        }

    }

    public void index(TicketModel ticket) {
        try {
            IndexWriter writer = this.getWriter();
            this.delete(ticket.repository, ticket.number, writer);
            Document doc = this.ticketToDoc(ticket);
            writer.addDocument(doc);
            writer.commit();
            this.closeSearcher();
        } catch (Exception var4) {
            this.log.error("error", var4);
        }

    }

    public boolean delete(TicketModel ticket) {
        try {
            IndexWriter writer = this.getWriter();
            return this.delete(ticket.repository, ticket.number, writer);
        } catch (Exception var3) {
            this.log.error("Failed to delete ticket " + ticket.number, var3);
            return false;
        }
    }

    private boolean delete(String repository, long ticketId, IndexWriter writer) throws Exception {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        QueryParser qp = new QueryParser(Lucene.did.name(), analyzer);
        BooleanQuery query = new BooleanQuery();
        query.add(qp.parse(StringUtils.getSHA1(repository + ticketId)), Occur.MUST);
        int numDocsBefore = writer.numDocs();
        writer.deleteDocuments(new Query[]{query});
        writer.commit();
        this.closeSearcher();
        int numDocsAfter = writer.numDocs();
        if (numDocsBefore == numDocsAfter) {
            this.log.debug(MessageFormat.format("no records found to delete in {0}", repository));
            return false;
        } else {
            this.log.debug(MessageFormat.format("deleted {0} records in {1}", numDocsBefore - numDocsAfter, repository));
            return true;
        }
    }

    public boolean hasTickets(RepositoryModel repository) {
        return !this.queryFor(Lucene.rid.matches(repository.getRID()), 1, 0, (String) null, true).isEmpty();
    }

    public List<QueryResult> searchFor(RepositoryModel repository, String text, int page, int pageSize) {
        if (StringUtils.isEmpty(text)) {
            return Collections.emptyList();
        } else {
            Set<QueryResult> results = new LinkedHashSet();
            StandardAnalyzer analyzer = new StandardAnalyzer();

            try {
                BooleanQuery query = new BooleanQuery();
                QueryParser qp = new QueryParser(Lucene.title.name(), analyzer);
                qp.setAllowLeadingWildcard(true);
                query.add(qp.parse(text), Occur.SHOULD);
                qp = new QueryParser(Lucene.body.name(), analyzer);
                qp.setAllowLeadingWildcard(true);
                query.add(qp.parse(text), Occur.SHOULD);
                qp = new QueryParser(Lucene.content.name(), analyzer);
                qp.setAllowLeadingWildcard(true);
                query.add(qp.parse(text), Occur.SHOULD);
                IndexSearcher searcher = this.getSearcher();
                Query rewrittenQuery = searcher.rewrite(query);
                this.log.debug(rewrittenQuery.toString());
                TopScoreDocCollector collector = TopScoreDocCollector.create(5000, null);
                searcher.search(rewrittenQuery, collector);
                int offset = Math.max(0, (page - 1) * pageSize);
                ScoreDoc[] hits = collector.topDocs(offset, pageSize).scoreDocs;

                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document doc = searcher.doc(docId);
                    QueryResult result = this.docToQueryResult(doc);
                    if (repository == null || result.repository.equalsIgnoreCase(repository.name)) {
                        results.add(result);
                    }
                }
            } catch (Exception var18) {
                this.log.error(MessageFormat.format("Exception while searching for {0}", text), var18);
            }

            return new ArrayList(results);
        }
    }

    public List<QueryResult> queryFor(String queryText, int page, int pageSize, String sortBy, boolean desc) {
        if (StringUtils.isEmpty(queryText)) {
            return Collections.emptyList();
        } else {
            Set<QueryResult> results = new LinkedHashSet();
            StandardAnalyzer analyzer = new StandardAnalyzer();

            try {
                QueryParser qp = new QueryParser(Lucene.content.name(), analyzer);
                Query query = qp.parse(queryText);
                IndexSearcher searcher = this.getSearcher();
                Query rewrittenQuery = searcher.rewrite(query);
                this.log.debug(rewrittenQuery.toString());
                Sort sort;
                if (sortBy == null) {
                    sort = new Sort(Lucene.created.asSortField(desc));
                } else {
                    sort = new Sort(Lucene.fromString(sortBy).asSortField(desc));
                }

                int maxSize = 5000;
                TopFieldDocs docs = searcher.search(rewrittenQuery, (Filter) null, maxSize, sort, false, false);
                int size = pageSize <= 0 ? maxSize : pageSize;
                int offset = Math.max(0, (page - 1) * size);
                ScoreDoc[] hits = this.subset(docs.scoreDocs, offset, size);

                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    Document doc = searcher.doc(docId);
                    QueryResult result = this.docToQueryResult(doc);
                    result.docId = docId;
                    result.totalResults = docs.totalHits;
                    results.add(result);
                }
            } catch (Exception var22) {
                this.log.error(MessageFormat.format("Exception while searching for {0}", queryText), var22);
            }

            return new ArrayList(results);
        }
    }

    private ScoreDoc[] subset(ScoreDoc[] docs, int offset, int size) {
        ScoreDoc[] set;
        if (docs.length >= offset + size) {
            set = new ScoreDoc[size];
            System.arraycopy(docs, offset, set, 0, set.length);
            return set;
        } else if (docs.length >= offset) {
            set = new ScoreDoc[docs.length - offset];
            System.arraycopy(docs, offset, set, 0, set.length);
            return set;
        } else {
            return new ScoreDoc[0];
        }
    }

    private IndexWriter getWriter() throws IOException {
        if (this.writer == null) {
            Directory directory = FSDirectory.open(this.luceneDir.toPath());
            if (!this.luceneDir.exists()) {
                this.luceneDir.mkdirs();
            }

            StandardAnalyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(OpenMode.CREATE_OR_APPEND);
            this.writer = new IndexWriter(directory, config);
        }

        return this.writer;
    }

    private synchronized void closeWriter() {
        try {
            if (this.writer != null) {
                this.writer.close();
            }
        } catch (Exception var5) {
            this.log.error("failed to close writer!", var5);
        } finally {
            this.writer = null;
        }

    }

    private IndexSearcher getSearcher() throws IOException {
        if (this.searcher == null) {
            this.searcher = new IndexSearcher(DirectoryReader.open(this.getWriter(), true));
        }

        return this.searcher;
    }

    private synchronized void closeSearcher() {
        try {
            if (this.searcher != null) {
                this.searcher.getIndexReader().close();
            }
        } catch (Exception var5) {
            this.log.error("failed to close searcher!", var5);
        } finally {
            this.searcher = null;
        }

    }

    private Document ticketToDoc(TicketModel ticket) {
        Document doc = new Document();
        this.toDocField(doc, Lucene.rid, StringUtils.getSHA1(ticket.repository));
        this.toDocField(doc, Lucene.did, StringUtils.getSHA1(ticket.repository + ticket.number));
        this.toDocField(doc, Lucene.project, ticket.project);
        this.toDocField(doc, Lucene.repository, ticket.repository);
        this.toDocField(doc, Lucene.number, ticket.number);
        this.toDocField(doc, Lucene.title, ticket.title);
        this.toDocField(doc, Lucene.body, ticket.body);
        this.toDocField(doc, Lucene.created, ticket.created);
        this.toDocField(doc, Lucene.createdby, ticket.createdBy);
        this.toDocField(doc, Lucene.updated, ticket.updated);
        this.toDocField(doc, Lucene.updatedby, ticket.updatedBy);
        this.toDocField(doc, Lucene.responsible, ticket.responsible);
        this.toDocField(doc, Lucene.milestone, ticket.milestone);
        this.toDocField(doc, Lucene.topic, ticket.topic);
        this.toDocField(doc, Lucene.status, ticket.status.name());
        this.toDocField(doc, Lucene.comments, ticket.getComments().size());
        this.toDocField(doc, Lucene.type, ticket.type == null ? null : ticket.type.name());
        this.toDocField(doc, Lucene.mergesha, ticket.mergeSha);
        this.toDocField(doc, Lucene.mergeto, ticket.mergeTo);
        this.toDocField(doc, Lucene.labels, StringUtils.flattenStrings(ticket.getLabels(), ";").toLowerCase());
        this.toDocField(doc, Lucene.participants, StringUtils.flattenStrings(ticket.getParticipants(), ";").toLowerCase());
        this.toDocField(doc, Lucene.watchedby, StringUtils.flattenStrings(ticket.getWatchers(), ";").toLowerCase());
        this.toDocField(doc, Lucene.mentions, StringUtils.flattenStrings(ticket.getMentions(), ";").toLowerCase());
        this.toDocField(doc, Lucene.votes, ticket.getVoters().size());
        this.toDocField(doc, Lucene.priority, ticket.priority.getValue());
        this.toDocField(doc, Lucene.severity, ticket.severity.getValue());
        List<String> attachments = new ArrayList();
        Iterator var4 = ticket.getAttachments().iterator();

        while (var4.hasNext()) {
            Attachment attachment = (Attachment) var4.next();
            attachments.add(attachment.name.toLowerCase());
        }

        this.toDocField(doc, Lucene.attachments, StringUtils.flattenStrings(attachments, ";"));
        List<Patchset> patches = ticket.getPatchsets();
        if (!patches.isEmpty()) {
            this.toDocField(doc, Lucene.patchsets, patches.size());
            Patchset patchset = (Patchset) patches.get(patches.size() - 1);
            String flat = patchset.number + ":" + patchset.rev + ":" + patchset.tip + ":" + patchset.base + ":" + patchset.commits;
            doc.add(new Field(Lucene.patchset.name(), flat, TextField.TYPE_STORED));
        }

        doc.add(new TextField(Lucene.content.name(), ticket.toIndexableString(), Store.NO));
        return doc;
    }

    private void toDocField(Document doc, Lucene lucene, Date value) {
        if (value != null) {
            doc.add(new LongField(lucene.name(), value.getTime(), Store.YES));
        }
    }

    private void toDocField(Document doc, Lucene lucene, long value) {
        doc.add(new LongField(lucene.name(), value, Store.YES));
    }

    private void toDocField(Document doc, Lucene lucene, int value) {
        doc.add(new IntField(lucene.name(), value, Store.YES));
    }

    private void toDocField(Document doc, Lucene lucene, String value) {
        if (!StringUtils.isEmpty(value)) {
            doc.add(new Field(lucene.name(), value, TextField.TYPE_STORED));
        }
    }

    private QueryResult docToQueryResult(Document doc) throws ParseException {
        QueryResult result = new QueryResult();
        result.project = this.unpackString(doc, Lucene.project);
        result.repository = this.unpackString(doc, Lucene.repository);
        result.number = this.unpackLong(doc, Lucene.number);
        result.createdBy = this.unpackString(doc, Lucene.createdby);
        result.createdAt = this.unpackDate(doc, Lucene.created);
        result.updatedBy = this.unpackString(doc, Lucene.updatedby);
        result.updatedAt = this.unpackDate(doc, Lucene.updated);
        result.title = this.unpackString(doc, Lucene.title);
        result.body = this.unpackString(doc, Lucene.body);
        result.status = Status.fromObject(this.unpackString(doc, Lucene.status), Status.New);
        result.responsible = this.unpackString(doc, Lucene.responsible);
        result.milestone = this.unpackString(doc, Lucene.milestone);
        result.topic = this.unpackString(doc, Lucene.topic);
        result.type = Type.fromObject(this.unpackString(doc, Lucene.type), Type.defaultType);
        result.mergeSha = this.unpackString(doc, Lucene.mergesha);
        result.mergeTo = this.unpackString(doc, Lucene.mergeto);
        result.commentsCount = this.unpackInt(doc, Lucene.comments);
        result.votesCount = this.unpackInt(doc, Lucene.votes);
        result.attachments = this.unpackStrings(doc, Lucene.attachments);
        result.labels = this.unpackStrings(doc, Lucene.labels);
        result.participants = this.unpackStrings(doc, Lucene.participants);
        result.watchedby = this.unpackStrings(doc, Lucene.watchedby);
        result.mentions = this.unpackStrings(doc, Lucene.mentions);
        result.priority = Priority.fromObject(this.unpackInt(doc, Lucene.priority), Priority.defaultPriority);
        result.severity = Severity.fromObject(this.unpackInt(doc, Lucene.severity), Severity.defaultSeverity);
        if (!StringUtils.isEmpty(doc.get(Lucene.patchset.name()))) {
            String[] values = doc.get(Lucene.patchset.name()).split(":", 5);
            Patchset patchset = new Patchset();
            patchset.number = Integer.parseInt(values[0]);
            patchset.rev = Integer.parseInt(values[1]);
            patchset.tip = values[2];
            patchset.base = values[3];
            patchset.commits = Integer.parseInt(values[4]);
            result.patchset = patchset;
        }

        return result;
    }

    private String unpackString(Document doc, Lucene lucene) {
        return doc.get(lucene.name());
    }

    private List<String> unpackStrings(Document doc, Lucene lucene) {
        return !StringUtils.isEmpty(doc.get(lucene.name())) ? StringUtils.getStringsFromValue(doc.get(lucene.name()), ";") : null;
    }

    private Date unpackDate(Document doc, Lucene lucene) {
        String val = doc.get(lucene.name());
        if (!StringUtils.isEmpty(val)) {
            long time = Long.parseLong(val);
            Date date = new Date(time);
            return date;
        } else {
            return null;
        }
    }

    private long unpackLong(Document doc, Lucene lucene) {
        String val = doc.get(lucene.name());
        if (StringUtils.isEmpty(val)) {
            return 0L;
        } else {
            long l = Long.parseLong(val);
            return l;
        }
    }

    private int unpackInt(Document doc, Lucene lucene) {
        String val = doc.get(lucene.name());
        if (StringUtils.isEmpty(val)) {
            return 0;
        } else {
            int i = Integer.parseInt(val);
            return i;
        }
    }

    public static enum Lucene {
        rid(SortField.Type.STRING),
        did(SortField.Type.STRING),
        project(SortField.Type.STRING),
        repository(SortField.Type.STRING),
        number(SortField.Type.LONG),
        title(SortField.Type.STRING),
        body(SortField.Type.STRING),
        topic(SortField.Type.STRING),
        created(SortField.Type.LONG),
        createdby(SortField.Type.STRING),
        updated(SortField.Type.LONG),
        updatedby(SortField.Type.STRING),
        responsible(SortField.Type.STRING),
        milestone(SortField.Type.STRING),
        status(SortField.Type.STRING),
        type(SortField.Type.STRING),
        labels(SortField.Type.STRING),
        participants(SortField.Type.STRING),
        watchedby(SortField.Type.STRING),
        mentions(SortField.Type.STRING),
        attachments(SortField.Type.INT),
        content(SortField.Type.STRING),
        patchset(SortField.Type.STRING),
        comments(SortField.Type.INT),
        mergesha(SortField.Type.STRING),
        mergeto(SortField.Type.STRING),
        patchsets(SortField.Type.INT),
        votes(SortField.Type.INT),
        priority(SortField.Type.INT),
        severity(SortField.Type.INT);

        final SortField.Type fieldType;

        private Lucene(SortField.Type fieldType) {
            this.fieldType = fieldType;
        }

        public String colon() {
            return this.name() + ":";
        }

        public String matches(String value) {
            if (StringUtils.isEmpty(value)) {
                return "";
            } else {
                boolean not = value.charAt(0) == '!';
                return not ? "!" + this.name() + ":" + this.escape(value.substring(1)) : this.name() + ":" + this.escape(value);
            }
        }

        public String doesNotMatch(String value) {
            return StringUtils.isEmpty(value) ? "" : "NOT " + this.name() + ":" + this.escape(value);
        }

        public String isNotNull() {
            return this.matches("[* TO *]");
        }

        public SortField asSortField(boolean descending) {
            return new SortField(this.name(), this.fieldType, descending);
        }

        private String escape(String value) {
            if (value.charAt(0) != '"') {
                char[] var2 = value.toCharArray();
                int var3 = var2.length;

                for (int var4 = 0; var4 < var3; ++var4) {
                    char c = var2[var4];
                    if (!Character.isLetterOrDigit(c)) {
                        return "\"" + value + "\"";
                    }
                }
            }

            return value;
        }

        public static Lucene fromString(String value) {
            Lucene[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Lucene field = var1[var3];
                if (field.name().equalsIgnoreCase(value)) {
                    return field;
                }
            }

            return created;
        }
    }
}
