import React from "react";
import PropTypes from "prop-types";
import {Breadcrumb, Button, Card, Layout} from "element-react";
import {Link} from "react-router-dom";
import AceEditor from "react-ace";

import "brace";
import "brace/mode/java";
import "brace/mode/javascript";
import "brace/mode/html";
import "brace/mode/css";
import "brace/theme/github";

export default class Editor extends React.Component {
    constructor(props) {
        super(props);
        const {repositoryName, branch, name} = props.location.state;
        const nameArray = name.split(".");
        const fileExt = nameArray.length > 1 ? nameArray[nameArray.length - 1] : "text";
        this.state = {
            path: props.params.path,
            repositoryName,
            branch,
            name,
            content: "",
            mode: fileExt
        };
    }

    componentDidMount() {
        fetch(`/api/repository/file/${this.state.repositoryName}/${this.state.branch}/${this.state.path}`).then((data) => {
            this.setState({content: data.content});
        });
    }

    onChange(content) {
        this.setState({content});
    }

    render() {
        return (
            <div>
                <Layout.Row>
                    <Layout.Col span="16">
                        <Breadcrumb separator="/">
                            <Breadcrumb.Item><Link to="/">Home</Link></Breadcrumb.Item>
                            <Breadcrumb.Item><Link to="/project">Project</Link></Breadcrumb.Item>
                            <Breadcrumb.Item>Project {this.state.id}</Breadcrumb.Item>
                        </Breadcrumb>
                    </Layout.Col>
                    <Layout.Col span="8">
                        <div className="head-operation">
                            <Button class="primary" size="small">Save </Button>
                        </div>
                    </Layout.Col>
                </Layout.Row>
                <Layout.Row>
                    <Layout.Col span="24">
                        <Card>
                            <AceEditor
                                mode={this.state.mode}
                                theme="github"
                                value={this.state.content}
                                onChange={value => this.onChange(value)}
                                name="UNIQUE_ID_OF_DIV"
                                editorProps={{$blockScrolling: true}}
                            />
                        </Card>
                    </Layout.Col>
                </Layout.Row>
            </div>
        );
    }

}

Editor.propTypes = {
    location: PropTypes.object,
    params: PropTypes.object
};
